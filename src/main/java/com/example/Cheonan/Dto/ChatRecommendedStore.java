package com.example.Cheonan.Dto;

import com.example.Cheonan.Entity.Store;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ChatRecommendedStore {
    @JsonProperty("place_name")
    private String name;

    @JsonProperty("road_address_name")
    private String address;

    @JsonProperty("category_name")
    private String category1;

    private String category2;

    @JsonProperty("place_url")
    private String kakaoLink;

    @JsonProperty("phone")
    private String phoneNumber;
    private Double rating;

    @JsonProperty("x")
    @Schema(description = "경도(Longitude), Kakao x", example = "127.145930169542")
    private Double x;

    @JsonProperty("y")
    @Schema(description = "위도(Latitude), Kakao y", example = "36.8155555448762")
    private Double y;

    public static ChatRecommendedStore fromEntity(Store s) {
        if (s == null) return null;
        return ChatRecommendedStore.builder()
                .name(s.getName())
                .address(s.getAddress())
                .category1(s.getCategory1())
                .category2(s.getCategory2())
                .kakaoLink(s.getKakaoLink())
                .phoneNumber(s.getPhoneNumber())
                .rating(s.getRating())
                .x(s.getY())
                .y(s.getX())
                .build();
    }
}

