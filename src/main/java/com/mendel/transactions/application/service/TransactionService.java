package com.mendel.transactions.application.service;

import com.mendel.transactions.api.dto.TransactionUpsertRequestDTO;
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
        // TODO: validar parent existe cuando se arme el grafo.
        Transaction tx = new Transaction(id, request.amount(), request.type(), request.parentId());
        repository.upsert(tx);
    }

    public Transaction getById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
    }

    public Set<Long> getIdsByType(String type) {
        return repository.findIdsByType(type);
    }

    public double sum(long transactionId) {
        //TODO: si no existe, error (luego lo mapeamos a 404)
        Transaction root = repository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        double total = 0.0;

        var stack = new ArrayDeque<Long>();
        var visited = new HashSet<Long>();

        stack.push(root.id());

        while (!stack.isEmpty()) {
            long currentId = stack.pop();
            if (!visited.add(currentId)) {
                continue; // evita loops si alguna vez hubiera ciclo
            }
            Transaction current = repository.findById(currentId)
                    .orElseThrow(() -> new IllegalStateException("Missing transaction in graph: " + currentId));
            total += current.amount();
            for (Long childId : repository.findChildrenIds(currentId)) {
                stack.push(childId);
            }
        }
        return total;
    }
}
