package com.mendel.transactions.application.port;

import com.mendel.transactions.domain.model.Transaction;

import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> findById(long id);
    void upsert(Transaction transaction);
}
