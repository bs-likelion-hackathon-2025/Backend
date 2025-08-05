package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.KakaoResponse;
import com.example.Cheonan.Dto.PlaceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KakaoMapService {

    private final RestTemplate restTemplate;
    private final String kakaoApiKey;

    // ✅ 실제 검색 가능한 키워드 중심 매핑
    private static final Map<String, String> categoryMap = Map.ofEntries(
            Map.entry("한식", "한식"),
            Map.entry("중식", "중식당"),
            Map.entry("일식", "스시"),
            Map.entry("초밥", "초밥"),
            Map.entry("양식", "파스타"),
            Map.entry("고기", "삼겹살"),
            Map.entry("치킨", "치킨집"),
            Map.entry("회", "횟집"),
            Map.entry("뷔페", "뷔페")
    );

    public KakaoMapService(RestTemplate restTemplate,
                           @Value("${kakao.rest.api.key}") String kakaoApiKey) {
        this.restTemplate = restTemplate;
        this.kakaoApiKey = kakaoApiKey;
    }

    public ResponseEntity<?> searchPlacesByCategory(String categoryCode, String x, String y) {
        try {
            // 좌표가 null이거나 비어 있으면 기본 좌표 사용 (천안시청)
            if (x == null || x.isBlank()) x = "127.1465";
            if (y == null || y.isBlank()) y = "36.8151";

            String url = "https://dapi.kakao.com/v2/local/search/category.json"
                    + "?category_group_code=" + categoryCode
                    + "&x=" + x
                    + "&y=" + y
                    + "&radius=20000"
                    + "&sort=distance"
                    + "&size=15";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.set("User-Agent", "Mozilla/5.0");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<KakaoResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    KakaoResponse.class
            );

            KakaoResponse body = response.getBody();

            if (body == null || body.getDocuments() == null || body.getDocuments().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "message", "검색 결과가 없습니다.",
                        "data", List.of()
                ));
            }

            List<PlaceDto> result = body.getDocuments().stream()
                    .map(doc -> new PlaceDto(
                            doc.getPlaceName(),
                            doc.getAddressName(),
                            doc.getPhone(),
                            doc.getRoadAddressName(),
                            doc.getPlaceUrl(),
                            doc.getX(),
                            doc.getY()
                    ))
                    .limit(5) // 거리순 5개만
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "message", "성공",
                    "data", result
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "서버 오류 발생",
                    "data", List.of()
            ));
        }
    }
}