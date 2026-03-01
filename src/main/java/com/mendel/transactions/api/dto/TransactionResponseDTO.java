package com.mendel.transactions.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionResponseDTO(
        long id,
        double amount,
        String type,
        @JsonProperty("parent_id") Long parentId
) {
}