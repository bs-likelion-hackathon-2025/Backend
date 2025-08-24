// src/main/java/com/example/Cheonan/Controller/ChatTwoStepController.java
package com.example.Cheonan.Controller;

import com.example.Cheonan.Dto.*;
import com.example.Cheonan.Service.ChatTwoStepService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<ChatRecommendedStore> recommended = service.getStores(requestId).getStores();
        ChatStoresResponse response = ChatStoresResponse.builder()
                .stores(recommended)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "3단계: 추천 가게 선택",
            description = "사용자가 추천된 가게 목록 중 하나를 선택하면 호출합니다. " +
                    "선택한 가게를 세션에 기록하고, 선택 완료 메시지를 반환합니다."
    )
    @PostMapping("/select")
    public ResponseEntity<ChatReplyResponse> selectStore(@Valid @RequestBody ChatSelectRequest req) {
        ChatReplyResponse reply = service.handleStoreSelection(req.getRequestId(), req.getStoreName());
        return ResponseEntity.ok(reply);
    }
}