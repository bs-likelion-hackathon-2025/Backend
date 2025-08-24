package com.example.Cheonan.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatRequest { // 챗봇 사용자 요청 메세지

    private String requestId; // 기존 세션 ID (없으면 새 세션 생성)

    @NotBlank(message = "메시지는 비어 있을 수 없습니다.")
    private String message;
}