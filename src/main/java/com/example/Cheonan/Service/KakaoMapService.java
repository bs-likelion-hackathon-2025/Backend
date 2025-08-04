package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.KakaoResponse;
import com.example.Cheonan.Dto.PlaceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KakaoMapService {

    private final RestTemplate restTemplate;
    private final String kakaoApiKey;

    public KakaoMapService(RestTemplate restTemplate,
                           @Value("${kakao.rest.api.key}") String kakaoApiKey) {
        this.restTemplate = restTemplate;
        this.kakaoApiKey = kakaoApiKey;
    }

    public List<PlaceDto> searchPlaces(String category) {
        try {
            // 검색어는 "천안시 + 카테고리명"
            String query = "천안시 " + category;
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url = "https://dapi.kakao.com/v2/local/search/keyword.json"
                    + "?query=" + encodedQuery
                    + "&category_group_code=FD6"
                    + "&size=5"; // 최대 5개

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<KakaoResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    KakaoResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getDocuments().stream()
                        .map(doc -> new PlaceDto(
                                doc.getPlaceName(),
                                doc.getAddressName(),
                                doc.getPhone(),
                                doc.getRoadAddressName(),
                                doc.getPlaceUrl(),
                                doc.getX(),
                                doc.getY()
                        ))
                        .collect(Collectors.toList());
            } else {
                return List.of(); // 빈 리스트 반환
            }

        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // 예외 발생 시 빈 리스트 반환
        }
    }
}