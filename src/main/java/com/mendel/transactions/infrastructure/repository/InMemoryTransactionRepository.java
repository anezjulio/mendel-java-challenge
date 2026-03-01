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
    private final ConcurrentHashMap<Long, Set<Long>> childrenByParent = new ConcurrentHashMap<>();


    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void upsert(Transaction tx) {
        Transaction previous = byId.put(tx.id(), tx);

        // limpiar índices si era update
        if (previous != null) {
            // type index
            Set<Long> oldTypeSet = idsByType.get(previous.type());
            if (oldTypeSet != null) oldTypeSet.remove(previous.id());

            // parent->children index
            if (previous.parentId() != null) {
                Set<Long> oldChildren = childrenByParent.get(previous.parentId());
                if (oldChildren != null) oldChildren.remove(previous.id());
            }
        }

        //  agregar a type index
        idsByType.computeIfAbsent(tx.type(), k -> ConcurrentHashMap.newKeySet())
                .add(tx.id());

        //  agregar a parent->children index
        if (tx.parentId() != null) {
            childrenByParent
                    .computeIfAbsent(tx.parentId(), k -> ConcurrentHashMap.newKeySet())
                    .add(tx.id());
        }
    }

    @Override
    public Set<Long> findIdsByType(String type) {
        return idsByType.getOrDefault(type, Collections.emptySet());
    }

    @Override
    public Set<Long> findChildrenIds(long parentId) {
        return childrenByParent.getOrDefault(parentId, Collections.emptySet());
    }

}
