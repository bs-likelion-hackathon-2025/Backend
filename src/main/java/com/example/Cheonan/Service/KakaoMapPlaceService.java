package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.KakaoDocument;
import com.example.Cheonan.Dto.KakaoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class KakaoMapPlaceService {

    private final RestTemplate restTemplate;
    private final String kakaoApiKey;

    public KakaoMapPlaceService(RestTemplate restTemplate,
                                @Value("${kakao.rest.api.key}") String kakaoApiKey) {
        this.restTemplate = restTemplate;
        this.kakaoApiKey = kakaoApiKey;
    }

    // 정확도순 전용 (천안 내 식당, query 반영)
    public ResponseEntity<?> searchAccuracy(String query,
                                            Integer radius, Integer size, Integer page,
                                            String categoryGroupCode) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "query는 필수입니다."));
        }

        // 천안시청 좌표
        String cheonanX = "127.1579";
        String cheonanY = "36.8151";

        // 반경: 사용자가 주면 반영, 없으면 10000m
        Integer effectiveRadius = (radius == null) ? 10000 : radius;

        // 카테고리는 무조건 식당(FD6)
        String effectiveGroup = "FD6";

        return doSearch(
                query,
                cheonanX,
                cheonanY,
                effectiveRadius,
                size,
                page,
                "accuracy",
                effectiveGroup
        );
    }

    // 거리순 전용 (x,y 필수)
    public ResponseEntity<?> searchDistance(String query, String x, String y,
                                            Integer radius, Integer size, Integer page,
                                            String categoryGroupCode) {
        if (x == null || x.isBlank() || y == null || y.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "x(경도), y(위도)는 필수입니다."));
        }
        return doSearch(query, x, y, radius, size, page, "distance", categoryGroupCode);
    }

    // 공통 구현
    private ResponseEntity<?> doSearch(String query, String x, String y,
                                       Integer radius, Integer size, Integer page,
                                       String sort, String categoryGroupCode) {
        try {
            if (query == null || query.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "query는 필수입니다."));
            }

            int s = (size == null || size < 1) ? 15 : Math.min(size, 14);     // 1~14
            int p = (page == null || page < 1) ? 1 : Math.min(page, 42);     // 1~42
            Integer r = (radius == null) ? null : Math.max(0, Math.min(radius, 20000)); // 0~20000
            String sortParam = (sort == null || sort.isBlank()) ? "accuracy" : sort;

            UriComponentsBuilder b = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .queryParam("size", s)
                    .queryParam("page", p)
                    .queryParam("sort", sortParam);

            if ("distance".equals(sortParam)) {
                if (x == null || x.isBlank() || y == null || y.isBlank()) {
                    return ResponseEntity.badRequest().body(Map.of("message", "거리순은 x(경도), y(위도)가 필수입니다."));
                }
            }

            if (x != null && !x.isBlank() && y != null && !y.isBlank()) {
                b.queryParam("x", x).queryParam("y", y);
            }
            if (r != null) b.queryParam("radius", r);
            if (categoryGroupCode != null && !categoryGroupCode.isBlank()) {
                b.queryParam("category_group_code", categoryGroupCode);
            }

            URI uri = b.build(false).encode(StandardCharsets.UTF_8).toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            ResponseEntity<KakaoResponse> resp = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers), KakaoResponse.class
            );
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류 발생: " + e.getMessage()));
        }
    }

    }
