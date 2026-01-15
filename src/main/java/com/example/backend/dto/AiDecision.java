package com.example.backend.dto;

public record AiDecision(
        boolean approved,
        String reasoning,
        Integer resolvedProductId,
        int resolvedQuantity) {
}
