package com.example.Cheonan.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class KakaoMapService {

    private final RestTemplate restTemplate;
    private final String kakaoApiKey;

    public KakaoMapService(RestTemplate restTemplate,
                           @Value("${kakao.rest.api.key}") String kakaoApiKey) {
        this.restTemplate = restTemplate;
        this.kakaoApiKey = kakaoApiKey;
    }

    /**
     * 키워드로 장소 검색 (이미지 제외)
     */
    public ResponseEntity<?> searchKeyword(String query,
                                           String x, String y,
                                           Integer radius,
                                           String categoryGroupCode) {
        try {
            if (query == null || query.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "query는 필수입니다."));
            }

            int size = 5;
            String sortParam = "distance";
            Integer r = (radius == null) ? null : Math.max(0, Math.min(radius, 20000));

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .queryParam("size", size)
                    .queryParam("sort", sortParam);

            builder.queryParam("x", x)
                    .queryParam("y", y);

            if (r != null) builder.queryParam("radius", r);
            if (categoryGroupCode != null && !categoryGroupCode.isBlank()) {
                builder.queryParam("category_group_code", categoryGroupCode);
            }

            URI uri = builder.build(false).encode(StandardCharsets.UTF_8).toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            return ResponseEntity.ok(resp.getBody());

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

    /**
     * 카카오 이미지 검색 API 호출 (장소명으로 1장 이미지 가져오기)
     */
    public String searchImage(String query) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/search/image")
                    .queryParam("query", query)
                    .queryParam("size", 1)
                    .queryParam("sort", "accuracy");

            URI uri = builder.build(false).encode(StandardCharsets.UTF_8).toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("documents")) {
                List<Map<String, Object>> docs = (List<Map<String, Object>>) body.get("documents");
                if (!docs.isEmpty()) {
                    return (String) docs.get(0).get("image_url");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}