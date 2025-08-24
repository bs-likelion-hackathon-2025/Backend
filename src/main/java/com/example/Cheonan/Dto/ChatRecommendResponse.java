package com.example.Cheonan.Dto;

import lombok.*;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRecommendResponse { // 챗봇 추천 가게 DTO
    private String reply;
    private IntentResult intent;
    private List<ChatRecommendedStore> stores;
}