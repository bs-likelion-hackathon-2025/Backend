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
        try {
            String category = body.get("category");
            String x = body.get("x");
            String y = body.get("y");

            // 400 Bad Request 처리
            if (category == null || x == null || y == null) {
                return ResponseEntity
                        .badRequest()
                        .body("요청에 필요한 파라미터(category, x, y)가 누락되었습니다.");
            }

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

            String categoryCode = categoryCodeMap.getOrDefault(category, "FD6");

            // 200 OK 또는 kakaoMapService 내부에서 예외 발생 시 500 처리
            return kakaoMapService.searchPlacesByCategory(categoryCode, x, y);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류 발생: " + e.getMessage());
        }
    }
}