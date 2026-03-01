package com.mendel.transactions.api.controller;

import com.mendel.transactions.api.dto.StatusResponseDTO;
import com.mendel.transactions.api.dto.TransactionResponseDTO;
import com.mendel.transactions.api.dto.TransactionUpsertRequestDTO;
import com.mendel.transactions.application.service.TransactionService;
import com.mendel.transactions.domain.model.Transaction;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PutMapping("/{transactionId}")
    public StatusResponseDTO upsert(
            @PathVariable long transactionId,
            @RequestBody @Valid TransactionUpsertRequestDTO request
    ) {
        service.upsert(transactionId, request);
        return new StatusResponseDTO("ok");
    }

    // Este GET no está en el enunciado, pero se usara para probar rápido mientras tanto.
    @GetMapping("/{transactionId}")
    public TransactionResponseDTO getById(@PathVariable long transactionId) {
        Transaction tx = service.getById(transactionId);
        return new TransactionResponseDTO(tx.id(), tx.amount(), tx.type(), tx.parentId());
    }

}
