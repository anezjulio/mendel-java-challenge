package com.mendel.transactions.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionUpsertRequestDTO (
        @NotNull Double amount,
        @NotBlank String type,
        @JsonProperty("parent_id") Long parentId
){
}
