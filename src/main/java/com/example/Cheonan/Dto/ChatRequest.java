package com.example.Cheonan.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatRequest { // 챗봇 사용자 요청 메세지

    @NotBlank(message = "메시지는 비어 있을 수 없습니다.")
    private String message;
}
