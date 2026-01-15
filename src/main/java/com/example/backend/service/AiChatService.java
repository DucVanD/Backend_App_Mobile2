package com.example.backend.service;

import com.example.backend.dto.ChatMessageDto;
import com.example.backend.dto.ChatResponse;
import java.util.List;

public interface AiChatService {
    /**
     * Send a message to AI and get response
     * 
     * @param message User's message
     * @param userId  Optional user ID for personalized responses
     * @param history Optional conversation history
     * @return AI response
     */
    ChatResponse chat(String message, Integer userId, List<ChatMessageDto> history) throws Exception;
}
