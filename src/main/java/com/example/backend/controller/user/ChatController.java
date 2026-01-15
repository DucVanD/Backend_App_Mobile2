package com.example.backend.controller.user;

import com.example.backend.dto.ChatRequest;
import com.example.backend.dto.ChatResponse;
import com.example.backend.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * Test Chat API
     * GET /api/chat
     */
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Chat API is working! Use POST to chat.");
    }

    /**
     * Chat with AI assistant
     * POST /api/chat
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        System.out.println(">>> AI Chat Request: " + (request != null ? request.getMessage() : "NULL"));

        if (request == null || request.getMessage() == null) {
            return ResponseEntity.badRequest().body(new ChatResponse("Message is required"));
        }

        try {
            ChatResponse response = aiChatService.chat(request.getMessage(), request.getUserId(), request.getHistory());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println(">>> AI Chat Error: " + e.getMessage());
            e.printStackTrace();
            ChatResponse errorResponse = new ChatResponse(
                    "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.");
            return ResponseEntity.ok(errorResponse);
        }
    }
}
