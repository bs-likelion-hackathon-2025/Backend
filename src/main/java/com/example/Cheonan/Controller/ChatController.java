package com.example.Cheonan.Controller;

import com.example.Cheonan.Dto.ChatRequest;
import com.example.Cheonan.Dto.ChatResponse;
import com.example.Cheonan.Entity.ChatMessage;
import com.example.Cheonan.Service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService; // 의존성 주입

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam String userId) {
        List<ChatMessage> history = chatService.getChatHistory(userId);
        return ResponseEntity.ok(history);
    }
}
