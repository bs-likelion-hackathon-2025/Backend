package com.example.Cheonan.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoMeta {
    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("pageable_count")
    private int pageableCount;

    @JsonProperty("is_end")
    private boolean isEnd;

    @JsonProperty("same_name")
    private SameName sameName;

    @Data
    public static class SameName {
        private java.util.List<String> region;     // ["강남구", ...]
        private String keyword;                    // 질의어(지역 제외)
        @JsonProperty("selected_region")
        private String selectedRegion;
    }
}