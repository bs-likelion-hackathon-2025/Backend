package com.example.Cheonan.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PlaceDetailService {

    private final RestTemplate restTemplate;
    private final String kakaoApiKey;

    public PlaceDetailService(RestTemplate restTemplate,
                              @Value("${kakao.rest.api.key}") String kakaoApiKey) {
        this.restTemplate = restTemplate;
        this.kakaoApiKey = kakaoApiKey;
    }

    /**
     * 공통 상세정보 조회
     */
    public Map<String, Object> getPlaceDetail(String placeName) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1) 카카오 장소 검색
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                    .queryParam("query", placeName)
                    .queryParam("size", 1);

            URI uri = builder.build(false).encode(StandardCharsets.UTF_8).toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = resp.getBody();

            if (body != null && body.containsKey("documents")) {
                List<Map<String, Object>> docs = (List<Map<String, Object>>) body.get("documents");
                if (!docs.isEmpty()) {
                    Map<String, Object> place = docs.get(0);
                    result.put("placeName", place.get("place_name"));
                    result.put("addressName", place.get("address_name"));           // 지번 주소
                    result.put("roadAddressName", place.get("road_address_name")); // 도로명 주소
                    result.put("phone", place.get("phone"));
                    result.put("x", place.get("x")); // 경도
                    result.put("y", place.get("y")); // 위도
                    result.put("category", place.get("category_name"));
                    result.put("categoryGroupName", place.get("category_group_name"));
                    result.put("placeUrl", place.get("place_url"));
                }
            }

            // 2) 카카오 이미지 검색 (대표 이미지 1장)
            String imageUrl = getPlaceImage(placeName);
            result.put("imageUrl", imageUrl);

        } catch (Exception e) {
            result.put("error", "상세정보 조회 실패: " + e.getMessage());
        }

        return result;
    }

    /**
     * 이미지 검색 전용 메서드
     */
    public String getPlaceImage(String query) {
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
            return null;
        }
        return null;
    }
}