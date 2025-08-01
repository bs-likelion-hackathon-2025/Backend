package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.ChatRequest;
import com.example.Cheonan.Dto.ChatResponse;
import com.example.Cheonan.Entity.ChatMessage;
import com.example.Cheonan.Repository.ChatMessageRepository;
import com.example.Cheonan.Util.GeminiClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final GeminiClient geminiClient;

    public ChatService(ChatMessageRepository chatMessageRepository, GeminiClient geminiClient) {
        this.chatMessageRepository = chatMessageRepository;
        this.geminiClient = geminiClient;
    }


    public ChatResponse processMessage(ChatRequest request) {
        String userInput = request.getMessage();
        String userId = request.getUserId();

        String aiResponse = geminiClient.getFoodRecommendation(userInput);

        ChatMessage chat = new ChatMessage(userId, userInput, aiResponse);
        chatMessageRepository.save(chat);


        return new ChatResponse(aiResponse);
    }

    public List<ChatMessage> getChatHistory(String userId) {
        return chatMessageRepository.findByUserId(userId);
    }
}
