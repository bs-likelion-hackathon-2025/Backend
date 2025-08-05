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
}