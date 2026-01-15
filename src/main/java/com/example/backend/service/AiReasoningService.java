package com.example.backend.service;

import com.example.backend.dto.AiDecision;

public interface AiReasoningService {
    AiDecision decideAddToCart(Integer userId, Integer productId, int quantity);
}
