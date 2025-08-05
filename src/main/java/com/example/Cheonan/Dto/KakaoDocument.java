package com.example.Cheonan.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoDocument {
    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("road_address_name")
    private String roadAddressName;

    @JsonProperty("place_url")
    private String placeUrl;

    @JsonProperty("x")
    private String x;

    @JsonProperty("y")
    private String y;

    @JsonProperty("category_group_code")
    private String categoryGroupCode;

    @JsonProperty("category_group_name")
    private String categoryGroupName;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("id")
    private String id;
}