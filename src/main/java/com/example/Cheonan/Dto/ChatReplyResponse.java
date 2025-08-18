package com.example.Cheonan.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChatReplyResponse {
    private String requestId;  // 이걸로 2단계에서 조회
    private String reply;      // 충청도 말투 답변
}