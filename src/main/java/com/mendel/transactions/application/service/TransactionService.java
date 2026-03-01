package com.mendel.transactions.application.service;

import com.mendel.transactions.api.dto.TransactionUpsertRequestDTO;
import com.mendel.transactions.application.port.TransactionRepository;
import com.mendel.transactions.domain.model.Transaction;
import org.springframework.stereotype.Service;

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
}
