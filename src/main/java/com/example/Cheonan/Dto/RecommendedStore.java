package com.example.Cheonan.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class RecommendedStore {
    private String name;
    private String address;
    private String category1;
    private String category2;
    private String googleLink;
    private String kakaoLink;
    private String phoneNumber;
    private Double rating;


    public RecommendedStore() {}

    public RecommendedStore( String name, String address, String category1, String category2, String googleLink, String kakaoLink, String phoneNumber, Double rating) {
        this.name=name;
        this.address=address;
        this.category1=category1;
        this.category2=category2;
        this.googleLink=googleLink;
        this.kakaoLink=kakaoLink;
        this.phoneNumber=phoneNumber;
        this.rating=rating;
    }
}
