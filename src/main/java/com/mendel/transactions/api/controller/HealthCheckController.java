package com.mendel.transactions.api.controller;

import com.mendel.transactions.api.dto.StatusResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public StatusResponseDTO health(){
        return new StatusResponseDTO("ok");
    }

}
