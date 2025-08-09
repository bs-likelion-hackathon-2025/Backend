// src/main/java/com/example/Cheonan/Controller/FoodRecommendationController.java
package com.example.Cheonan.Controller;

import com.example.Cheonan.Service.KakaoMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "음식 추천 API", description = "카카오맵 키워드 검색 프록시")
public class FoodRecommendController {

    private final KakaoMapService kakaoMapService;

    public FoodRecommendController(KakaoMapService kakaoMapService) {
        this.kakaoMapService = kakaoMapService;
    }

    @Operation(
            summary = "키워드로 장소 검색",
            description = """
                    카카오 키워드 검색(/v2/local/search/keyword.json)을 프록시합니다.
                    요청 바디 예:
                    {
                      "query": "고기",
                      "x": "127.0590",
                      "y": "37.5120",
                      "radius": 3000,
                      "size": 15,
                      "page": 1,
                      "sort": "distance",
                      "categoryGroupCode": "FD6"
                    }
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공(카카오 응답 meta/documents 그대로 전달)"),
            @ApiResponse(responseCode = "4xx", description = "카카오로부터의 에러를 그대로 전달"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody Map<String, Object> body) {
        String query = asString(body.get("query"));
        String category = asString(body.get("category")); // 과거 파라미터 호환

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
        Integer size = asInteger(body.get("size"));
        Integer page = asInteger(body.get("page"));
        String sort = asString(body.get("sort"));
        String group = asString(body.get("categoryGroupCode"));

        return kakaoMapService.searchKeyword(query, x, y, radius, size, page, sort, group);
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