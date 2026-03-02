package com.mendel.transactions.infrastructure.repository;

import com.mendel.transactions.application.port.TransactionRepository;
import com.mendel.transactions.domain.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<Long, Transaction> byId = new HashMap<>();
    private final Map<String, Set<Long>> idsByType = new HashMap<>();
    private final Map<Long, Set<Long>> childrenByParent = new HashMap<>();

    @Override
    public synchronized Optional<Transaction> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized void upsert(Transaction tx) {

        Transaction previous = byId.put(tx.id(), tx);

        // Cleanup si es update
        if (previous != null) {
            // limpiar índice de type
            Set<Long> oldTypeSet = idsByType.get(previous.type());
            if (oldTypeSet != null) {
                oldTypeSet.remove(previous.id());
                if (oldTypeSet.isEmpty()) {
                    idsByType.remove(previous.type());
                }
            }

            // limpiar índice parent->children
            if (previous.parentId() != null) {
                Set<Long> oldChildren = childrenByParent.get(previous.parentId());
                if (oldChildren != null) {
                    oldChildren.remove(previous.id());
                    if (oldChildren.isEmpty()) {
                        childrenByParent.remove(previous.parentId());
                    }
                }
            }
        }

        // agregar al índice por type
        idsByType
                .computeIfAbsent(tx.type(), k -> new HashSet<>())
                .add(tx.id());

        // agregar al índice parent->children
        if (tx.parentId() != null) {
            childrenByParent
                    .computeIfAbsent(tx.parentId(), k -> new HashSet<>())
                    .add(tx.id());
        }
    }

    @Override
    public synchronized Set<Long> findIdsByType(String type) {
        return new HashSet<>(idsByType.getOrDefault(type, Set.of()));
    }

    @Override
    public synchronized Set<Long> findChildrenIds(long parentId) {
        return new HashSet<>(childrenByParent.getOrDefault(parentId, Set.of()));
    }
}
