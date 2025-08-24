package com.example.Cheonan.Controller;

import com.example.Cheonan.Service.KakaoMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "음식 추천 API", description = "카카오맵 키워드 + 이미지 검색")
public class FoodRecommendController {

    private final KakaoMapService kakaoMapService;

    public FoodRecommendController(KakaoMapService kakaoMapService) {
        this.kakaoMapService = kakaoMapService;
    }

    @Operation(summary = "룰렛 결과 기반 음식점 5곳 거리순 추천")
    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody Map<String, Object> body) {
        String query = asString(body.get("query"));
        String category = asString(body.get("category"));
        String x = asString(body.get("x"));
        String y = asString(body.get("y"));
        Integer radius = asInteger(body.get("radius"));
        String group = asString(body.get("categoryGroupCode"));

        // 좌표 필수 확인
        if (x == null || x.isBlank() || y == null || y.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", -2, "msg", "Required Parameter (x, y)"));
        }

        // query 없으면 category 기반으로 변환
        if ((query == null || query.isBlank()) && category != null && !category.isBlank()) {
            query = switch (category) {
                case "고기" -> "고깃집";
                case "한식" -> "한식당";
                case "중식" -> "중식당";
                case "일식", "초밥" -> "초밥";
                case "양식" -> "파스타";
                case "치킨" -> "치킨집";
                case "회" -> "횟집";
                case "뷔페" -> "뷔페";
                case "카페" -> "카페";
                default -> category;
            };
        }

        return kakaoMapService.searchKeyword(query, x, y, radius, group);
    }



    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Integer asInteger(Object o) {
        if (o == null) return null;
        try {
            return Integer.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}