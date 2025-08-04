package com.example.Cheonan.Controller;

import com.example.Cheonan.Dto.PlaceDto;
import com.example.Cheonan.Service.KakaoMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "음식 추천 API", description = "카카오맵을 활용한 음식점 추천 API")
public class FoodRecommendationController {

    @Autowired
    private KakaoMapService kakaoMapService;

    @Operation(summary = "음식점 추천", description = "선택된 음식 카테고리에 따라 음식점을 추천합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천된 음식점 리스트 반환 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류"),
            @ApiResponse(responseCode = "500", description = "카카오 API 오류 또는 서버 내부 오류")
    })
    @PostMapping("/recommend")
    public ResponseEntity<List<PlaceDto>> recommendFood(@RequestBody Map<String, String> request) {
        String category = request.get("category"); // ex: 한식, 중식
        List<PlaceDto> places = kakaoMapService.searchPlaces(category);
        return ResponseEntity.ok(places);
    }
}