package com.mendel.transactions.application.service;

import com.mendel.transactions.api.dto.TransactionUpsertRequestDTO;
import com.mendel.transactions.api.exception.BadRequestException;
import com.mendel.transactions.api.exception.NotFoundException;
import com.mendel.transactions.application.port.TransactionRepository;
import com.mendel.transactions.domain.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public void upsert(long id, TransactionUpsertRequestDTO request) {
        Long parentId = request.parentId();

        if (parentId != null) {
            // parent debe existir
            if (repository.findById(parentId).isEmpty()) {
                throw new BadRequestException("Parent transaction not found: " + parentId);
            }

            // no puede ser su propio padre
            if (parentId.longValue() == id){
                throw new BadRequestException("Transaction cannot be its own parent: " + id);
            }

            // prevenir ciclo: parentId no puede ser un descendiente de id
            if (isDescendant(id, parentId)) {
                throw new BadRequestException("Cycle detected: cannot set parent_id " + parentId + " for transaction " + id);
            }
        }

        Transaction tx = new Transaction(id, request.amount(), request.type(), parentId);
        repository.upsert(tx);
    }

    public Transaction getById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }

    public Set<Long> getIdsByType(String type) {
        return repository.findIdsByType(type);
    }

    public double sum(long transactionId) {
        double total = 0.0;
        var stack = new ArrayDeque<Long>();
        var visited = new HashSet<Long>();

        stack.push(transactionId);

        while (!stack.isEmpty()) {
            long currentId = stack.pop();
            if (!visited.add(currentId)) continue;

            Transaction current = repository.findById(currentId)
                    .orElseThrow(() -> new NotFoundException("Transaction not found: " + currentId));

            total += current.amount();

            for (Long childId : repository.findChildrenIds(currentId)) {
                stack.push(childId);
            }
        }

        return total;
    }

    private boolean isDescendant(long rootId, long candidateDescendantId) {
        // desde rootId se busca candidateDescendantId en el sub-árbol actual
        var stack = new ArrayDeque<Long>();
        var visited = new HashSet<Long>();

        stack.push(rootId);

        while (!stack.isEmpty()) {
            long current = stack.pop();
            if (!visited.add(current)) continue;

            for (Long child : repository.findChildrenIds(current)) {
                if (child != null && child.longValue() == candidateDescendantId) return true;
                stack.push(child);
            }
        }
        return false;
    }
}
