package com.mendel.transactions.infrastructure.repository;

import com.mendel.transactions.application.port.TransactionRepository;
import com.mendel.transactions.domain.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final ConcurrentHashMap<Long, Transaction> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Long>> idsByType = new ConcurrentHashMap<>();

    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void upsert(Transaction tx) {
        // si existía, remover del type anterior (para no dejar basura)
        Transaction previous = byId.put(tx.id(), tx);
        if (previous != null) {
            Set<Long> oldSet = idsByType.get(previous.type());
            if (oldSet != null) oldSet.remove(previous.id());
        }

        idsByType.computeIfAbsent(tx.type(), k -> ConcurrentHashMap.newKeySet())
                .add(tx.id());
    }

    @Override
    public Set<Long> findIdsByType(String type) {
        return idsByType.getOrDefault(type, Collections.emptySet());
    }
}
