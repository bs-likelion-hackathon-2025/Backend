package com.example.Cheonan.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoDocument {

    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("road_address_name")
    private String roadAddressName;

    private String phone;

    @JsonProperty("place_url")
    private String placeUrl;

    @JsonProperty("X")
    private String X;

    @JsonProperty("Y")
    private String Y;
}