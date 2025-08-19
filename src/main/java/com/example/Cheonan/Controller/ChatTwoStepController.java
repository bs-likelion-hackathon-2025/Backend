// src/main/java/com/example/Cheonan/Controller/ChatTwoStepController.java
package com.example.Cheonan.Controller;

import com.example.Cheonan.Dto.ChatReplyResponse;
import com.example.Cheonan.Dto.ChatRequest;
import com.example.Cheonan.Dto.ChatStoresResponse;
import com.example.Cheonan.Service.ChatTwoStepService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Validated
public class ChatTwoStepController {

    private final ChatTwoStepService service;

    @Operation(summary = "1단계: 메시지 보내고 reply + requestId 받기")
    @PostMapping("/message")
    public ResponseEntity<ChatReplyResponse> message(@Valid @RequestBody ChatRequest req) {
        return ResponseEntity.ok(service.handleMessage(req));
    }

    @Operation(summary = "2단계: requestId로 가게 목록 받기")
    @GetMapping("/stores")
    public ResponseEntity<ChatStoresResponse> stores(@RequestParam @NotBlank String requestId) {
        return ResponseEntity.ok(service.getStores(requestId));
    }
}