package com.example.Cheonan.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ChatRecommendedStore {
    private String name;
    private String address;
    private String category1;
    private String category2;
    private String kakaoLink;
    private String phoneNumber;
    private Double rating;
}