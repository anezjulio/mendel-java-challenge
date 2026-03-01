package com.mendel.transactions.api.dto;

import java.util.List;

public record ErrorResponseDTO(
        String error,
        String message,
        List<String> details
) {
    public static ErrorResponseDTO of(String error, String message) {
        return new ErrorResponseDTO(error, message, List.of());
    }

    public static ErrorResponseDTO of(String error, String message, List<String> details) {
        return new ErrorResponseDTO(error, message, details);
    }
}