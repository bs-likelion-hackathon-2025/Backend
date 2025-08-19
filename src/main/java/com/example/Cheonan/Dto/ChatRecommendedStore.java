package com.example.Cheonan.Dto;

import com.example.Cheonan.Entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ChatRecommendedStore {
    private String name;
    private String address;
    private String category1;
    private String category2;
    private String kakaoLink;
    private String phoneNumber;
    private Double rating;

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
                .build();
    }
}