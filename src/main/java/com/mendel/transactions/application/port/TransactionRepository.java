package com.mendel.transactions.application.port;

import com.mendel.transactions.domain.model.Transaction;

import java.util.Optional;
import java.util.Set;

public interface TransactionRepository {
    Optional<Transaction> findById(long id);
    void upsert(Transaction transaction);
    Set<Long> findIdsByType(String type);
    Set<Long> findChildrenIds(long parentId);
}
