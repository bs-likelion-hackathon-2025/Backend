package com.example.Cheonan.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoDocument {
    private String id;

    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("category_group_code")
    private String categoryGroupCode;

    @JsonProperty("category_group_name")
    private String categoryGroupName;

    private String phone;

    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("road_address_name")
    private String roadAddressName;

    // 좌표 (카카오: x=경도, y=위도)
    private String x;
    private String y;

    @JsonProperty("place_url")
    private String placeUrl;

    // x,y를 넣었을 때만 존재 (문서상 선택 필드)
    private String distance;
}