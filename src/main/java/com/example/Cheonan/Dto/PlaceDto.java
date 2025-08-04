package com.example.Cheonan.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlaceDto {
    private String placeName;
    private String addressName;
    private String phone;
    private String roadAddressName;
    private String placeUrl;
    private String x; // 경도
    private String y; // 위도
}