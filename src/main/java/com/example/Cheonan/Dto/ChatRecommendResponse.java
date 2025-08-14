package com.example.Cheonan.Dto;

import com.example.Cheonan.Dto.IntentResult;
import com.example.Cheonan.Dto.RecommendedStore;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRecommendResponse {
    private String reply;
    private IntentResult intent;
    private List<RecommendedStore> stores;
}