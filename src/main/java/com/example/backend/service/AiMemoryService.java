package com.example.backend.service;

import java.util.List;

public interface AiMemoryService {
    void setLastSuggestedProducts(Integer userId, List<Integer> productIds);

    List<Integer> getLastSuggestedProducts(Integer userId);

    void clearMemory(Integer userId);
}
