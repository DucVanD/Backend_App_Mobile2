package com.example.backend.dto;

import java.util.List;

public class ChatRequest {
    private String message;
    private Integer userId;
    private List<ChatMessageDto> history;

    public ChatRequest() {
    }

    public ChatRequest(String message, Integer userId, List<ChatMessageDto> history) {
        this.message = message;
        this.userId = userId;
        this.history = history;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<ChatMessageDto> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessageDto> history) {
        this.history = history;
    }
}
