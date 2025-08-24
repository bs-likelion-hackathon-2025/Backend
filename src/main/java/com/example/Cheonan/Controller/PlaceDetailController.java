package com.example.Cheonan.Controller;

import com.example.Cheonan.Service.PlaceDetailService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PlaceDetailController {

    private final PlaceDetailService placeDetailService;

    public PlaceDetailController(PlaceDetailService placeDetailService) {
        this.placeDetailService = placeDetailService;
    }

    @Operation(
            summary = "공통 식당 상세페이지 조회",
            description = """
    특정 장소명을 기반으로 상세 정보를 조회합니다.
    - 요청 방식: GET
    - 파라미터: placeName (예: 스타벅스 천안신부점)
    - 반환: 장소명, 주소, 전화번호, 좌표(x,y), 카테고리, 이미지 URL, 카카오맵 상세페이지 URL
    """
    )
    @GetMapping("/place/detail")
    public ResponseEntity<?> getPlaceDetail(@RequestParam String placeName) {
        Map<String, Object> detail = placeDetailService.getPlaceDetail(placeName);

        if (detail.containsKey("error")) {
            return ResponseEntity.badRequest().body(detail);
        }
        return ResponseEntity.ok(detail);
    }
}