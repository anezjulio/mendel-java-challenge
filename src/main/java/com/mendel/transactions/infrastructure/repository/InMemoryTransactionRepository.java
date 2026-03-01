package com.mendel.transactions.infrastructure.repository;

import com.mendel.transactions.application.port.TransactionRepository;
import com.mendel.transactions.domain.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final ConcurrentHashMap<Long, Transaction> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void upsert(Transaction transaction) {
        byId.put(transaction.id(), transaction);
    }
}
