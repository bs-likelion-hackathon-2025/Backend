package com.example.Cheonan.Controller;

import com.example.Cheonan.Dto.PageResponse;
import com.example.Cheonan.Dto.StoreCardDto;
import com.example.Cheonan.Service.KakaoMapService;
import com.example.Cheonan.Service.MainStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> recommend(@Valid @RequestBody Map<String, Object> body) {
        String query = asString(body.get("query"));
        String category = asString(body.get("category"));

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

        String x = asString(body.get("x"));
        String y = asString(body.get("y"));
        Integer radius = asInteger(body.get("radius"));
        String group = asString(body.get("categoryGroupCode"));

        return kakaoMapService.searchKeyword(query, x, y, radius, group);
    }

    @Operation(summary = "특정 장소 이미지 가져오기")
    @GetMapping("/recommend/image")
    public ResponseEntity<?> getImage(@RequestParam String placeName) {
        String imageUrl = kakaoMapService.searchImage(placeName);
        if (imageUrl != null) {
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } else {
            return ResponseEntity.ok(Map.of("imageUrl", ""));
        }
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