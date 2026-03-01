package com.mendel.transactions.domain.model;

import java.util.Objects;

public record Transaction (
       long id,
       double amount,
       String type,
       Long parentId

){
    public Transaction {
        Objects.requireNonNull(type, "type must not be null");
    }
}
