package com.example.backend.service.impl;

import com.example.backend.service.AiMemoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiMemoryServiceImpl implements AiMemoryService {

    // Simple in-memory storage. For a production app with multiple instances,
    // this should be moved to Redis or a database.
    private final Map<Integer, List<Integer>> userProductMemory = new ConcurrentHashMap<>();

    @Override
    public void setLastSuggestedProducts(Integer userId, List<Integer> productIds) {
        if (userId == null)
            return;
        userProductMemory.put(userId, new ArrayList<>(productIds));
    }

    @Override
    public List<Integer> getLastSuggestedProducts(Integer userId) {
        if (userId == null)
            return new ArrayList<>();
        return userProductMemory.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public void clearMemory(Integer userId) {
        if (userId != null) {
            userProductMemory.remove(userId);
        }
    }
}
