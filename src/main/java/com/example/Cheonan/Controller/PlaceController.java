// src/main/java/com/example/Cheonan/Controller/PlaceController.java
package com.example.Cheonan.Controller;

import com.example.Cheonan.Service.KakaoMapPlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/place")
@Tag(name = "장소 검색 API", description = "카카오 키워드 검색 - 정확도순/거리순 분리")
public class PlaceController {

    private final KakaoMapPlaceService service;

    public PlaceController(KakaoMapPlaceService service) {
        this.service = service;
    }

    @Operation(
            summary = "정확도순 검색 (accuracy)",
            description = """
        카카오 키워드 검색을 정확도순으로 조회합니다.
        요청 바디 예:
        {
          "query": "한식",
          "radius": 3000,          // 선택(0~20000)
          "size": 15,              // 1~15
          "page": 1,               // 1~45
          "categoryGroupCode": "FD6"
        }
        """
    )
    @PostMapping("/search/accuracy")
    public ResponseEntity<?> searchAccuracy(@RequestBody Map<String, Object> body) {
        String query  = asString(body.get("query"));
        Integer radius= asInt(body.get("radius"));
        Integer size  = asInt(body.get("size"));
        Integer page  = asInt(body.get("page"));
        String group  = asString(body.get("categoryGroupCode"));
        return service.searchAccuracy(query, radius, size, page, group);
    }

    @Operation(
            summary = "거리순 검색 (distance)",
            description = """
        카카오 키워드 검색을 거리순으로 조회합니다. x(경도), y(위도) 필수.
        요청 바디 예:
        {
          "query": "한식",
          "x": "127.1458",
          "y": "36.8152",
          "radius": 3000,
          "size": 15,
          "page": 1,
          "categoryGroupCode": "FD6"
        }
        """
    )
    @PostMapping("/search/distance")
    public ResponseEntity<?> searchDistance(@RequestBody Map<String, Object> body) {
        String query  = asString(body.get("query"));
        String x      = asString(body.get("x"));   // 필수
        String y      = asString(body.get("y"));   // 필수
        Integer radius= asInt(body.get("radius"));
        Integer size  = asInt(body.get("size"));
        Integer page  = asInt(body.get("page"));
        String group  = asString(body.get("categoryGroupCode"));
        return service.searchDistance(query, x, y, radius, size, page, group);
    }

    private String asString(Object o) { return o == null ? null : String.valueOf(o); }
    private Integer asInt(Object o) {
        if (o == null) return null;
        try { return Integer.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}