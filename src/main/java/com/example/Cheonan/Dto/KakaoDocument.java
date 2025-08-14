package com.example.Cheonan.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "KakaoDocument", description = "카카오 장소 검색 단일 문서(장소) 응답")
public class KakaoDocument {

    @Schema(description = "장소 ID", example = "123456789")
    private String id;

    @JsonProperty("place_name")
    @Schema(description = "장소명", example = "햇살머믄꼬마김밥")
    private String placeName;

    @JsonProperty("category_name")
    @Schema(description = "카테고리명", example = "음식점 > 한식")
    private String categoryName;

    @JsonProperty("category_group_code")
    @Schema(description = "카테고리 그룹 코드", example = "FD6")
    private String categoryGroupCode;

    @JsonProperty("category_group_name")
    @Schema(description = "카테고리 그룹명", example = "음식점")
    private String categoryGroupName;

    @Schema(description = "전화번호", example = "041-585-7979")
    private String phone;

    @JsonProperty("address_name")
    @Schema(description = "지번 주소", example = "충남 천안시 서북구 성정동 124-2")
    private String addressName;

    @JsonProperty("road_address_name")
    @Schema(description = "도로명 주소", example = "충남 천안시 서북구 선영5길 28")
    private String roadAddressName;

    @Schema(description = "경도(Longitude), Kakao x", example = "127.145930169542")
    private String x;

    @Schema(description = "위도(Latitude), Kakao y", example = "36.8155555448762")
    private String y;

    @JsonProperty("place_url")
    @Schema(description = "카카오 장소 URL", example = "http://place.map.kakao.com/502112399")
    private String placeUrl;

    @Schema(description = "요청에 좌표(x,y) 포함 시 제공되는 거리(m) - 선택 필드", example = "153")
    private String distance;

    @JsonProperty("image_url")
    @Schema(description = "장소 관련 이미지 URL (추가 필드)", example = "https://example.com/image.jpg")
    private String imageUrl;  // camelCase 필드명으로 추가

}