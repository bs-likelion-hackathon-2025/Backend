// src/main/java/com/example/Cheonan/Service/KakaoMapService.java
package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.KakaoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

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
     * 카카오 키워드 검색 프록시 (GET)
     * - 수동 인코딩 금지(URLEncoder X), UriComponentsBuilder + encode 사용
     */
    public ResponseEntity<?> searchKeyword(String query,
                                           String x, String y,
                                           Integer radius, Integer size, Integer page,
                                           String sort, String categoryGroupCode) {
        try {
            if (query == null || query.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("message", "query는 필수입니다."));
            }

            // 카카오 제약
            int s = (size == null || size < 1) ? 15 : Math.min(size, 15);    // 1~15
            int p = (page == null || page < 1) ? 1  : Math.min(page, 45);    // 1~45
            Integer r = (radius == null) ? null : Math.max(0, Math.min(radius, 20000)); // 0~20000
            String sortParam = (sort == null || sort.isBlank()) ? "accuracy" : sort;    // accuracy|distance

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .queryParam("size", s)
                    .queryParam("page", p)
                    .queryParam("sort", sortParam);

            if (x != null && !x.isBlank() && y != null && !y.isBlank()) {
                builder.queryParam("x", x).queryParam("y", y);
            }
            if (r != null) builder.queryParam("radius", r);
            if (categoryGroupCode != null && !categoryGroupCode.isBlank()) {
                builder.queryParam("category_group_code", categoryGroupCode);
            }

            URI uri = builder.build(false)
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            // 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 호출
            ResponseEntity<KakaoResponse> resp = restTemplate.exchange(
                    uri, HttpMethod.GET, entity, KakaoResponse.class
            );

            return ResponseEntity.status(resp.getStatusCode())
                    .headers(copyContentType(resp.getHeaders()))
                    .body(resp.getBody());

        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "서버 오류 발생: " + e.getMessage()));
        }
    }

    private HttpHeaders copyContentType(HttpHeaders src) {
        HttpHeaders dst = new HttpHeaders();
        if (src.getContentType() != null) dst.setContentType(src.getContentType());
        return dst;
    }
}