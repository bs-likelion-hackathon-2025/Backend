package com.example.Cheonan.Controller;

import com.example.Cheonan.Service.KakaoMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "음식 추천 API", description = "카카오맵을 활용한 음식점 추천 API")
public class FoodRecommendationController {

    private final KakaoMapService kakaoMapService;
    private final RestTemplate restTemplate;
    private final String kakaoApiKey;

    public FoodRecommendationController(KakaoMapService kakaoMapService,
                                        RestTemplate restTemplate,
                                        @Value("${kakao.rest.api.key}") String kakaoApiKey) {
        this.kakaoMapService = kakaoMapService;
        this.restTemplate = restTemplate;
        this.kakaoApiKey = kakaoApiKey;
    }

    @Operation(summary = "음식점 추천", description = "선택된 음식 카테고리에 따라 음식점을 추천합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천된 음식점 리스트 반환 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류"),
            @ApiResponse(responseCode = "500", description = "카카오 API 오류 또는 서버 내부 오류")
    })
    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody Map<String, String> body) {
        String category = body.get("category");
        String x = body.get("x"); // 클라이언트에서 받은 경도
        String y = body.get("y"); // 클라이언트에서 받은 위도

        // category를 코드로 매핑 (예: 음식점 → FD6, 카페 → CE7)
        Map<String, String> categoryCodeMap = Map.ofEntries(
                Map.entry("음식점", "FD6"),
                Map.entry("한식", "FD6"),
                Map.entry("중식", "FD6"),
                Map.entry("일식", "FD6"),
                Map.entry("고기", "FD6"),
                Map.entry("양식", "FD6"),
                Map.entry("카페", "CE7"),
                Map.entry("병원", "HP8"),
                Map.entry("약국", "PM9"),
                Map.entry("관광명소", "AT4"),
                Map.entry("숙박", "AD5")
        );

        // category 코드 추출, 기본값 FD6
        String categoryCode = categoryCodeMap.getOrDefault(category, "FD6");

        // kakaoMapService 호출 (x, y 포함)
        return kakaoMapService.searchPlacesByCategory(categoryCode, x, y);
    }
}