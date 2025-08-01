package com.example.Cheonan.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";    private final RestTemplate restTemplate = new RestTemplate();

    public String getFoodRecommendation(String userMessage) {
        Map<String, Object> contentPart = Map.of("text", "다음 요청에 따라 음식을 간단히 2줄 요약해서 추천해줘: " + userMessage);
        Map<String, Object> content = Map.of("parts", List.of(contentPart));
        Map<String, Object> body = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            String urlWithKey = GEMINI_URL + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, request, Map.class);

            System.out.println("=== Gemini 응답 ===");
            System.out.println(response.getBody());

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                return (String) parts.get(0).get("text");
            }
            return "추천 결과가 비어 있어요.";
        } catch (Exception e) {
            e.printStackTrace();  // 실제 예외 스택 출력
            return "Gemini 추천에 실패했어요: " + e.getMessage();
        }
    }
}
