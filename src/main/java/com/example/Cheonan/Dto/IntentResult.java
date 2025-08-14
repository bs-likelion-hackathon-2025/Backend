// src/main/java/com/example/Cheonan/Dto/IntentResult.java
package com.example.Cheonan.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.List;

/**
 * LLM(예: Gemini)이 사용자 발화를 분석해 뽑아준 "의도/맥락" 묶음.
 * DB 저장 목적이 아니라 추천 단계의 필터/가중치에 쓰는 임시 데이터 컨테이너.
 * -> 일부 필드는 null/빈값이어도 안전하게 동작하도록 추천로직을 설계한다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // LLM이 넣은 예상 밖 필드 무시
public class IntentResult {
    private String category1;
    private List<String> category2Candidates; // 최대 3개
    private List<String> category3Candidates; // 최대 3개
    private List<String> category4Candidates;
}