package com.example.Cheonan.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(name = "KakaoMeta", description = "카카오 장소 검색 메타데이터")
public class KakaoMeta {

    @JsonProperty("total_count")
    @Schema(description = "검색어에 매칭된 전체 문서 수", example = "1234")
    private int totalCount;

    @JsonProperty("pageable_count")
    @Schema(description = "페이지네이션 가능한 문서 수(최대 45)", example = "45")
    private int pageableCount;

    @JsonProperty("is_end")
    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean isEnd;

    @JsonProperty("same_name")
    @Schema(description = "동일 명칭 관련 정보(지역/키워드 분해)")
    private SameName sameName;

    @Data
    @NoArgsConstructor
    @Schema(name = "KakaoMeta.SameName", description = "동일 명칭 보조 정보")
    public static class SameName {

        @ArraySchema(arraySchema = @Schema(description = "추정 지역 리스트"),
                schema = @Schema(example = "강남구"))
        private List<String> region;

        @Schema(description = "질의 키워드(지역 제외)", example = "김밥")
        private String keyword;

        @JsonProperty("selected_region")
        @Schema(description = "선택된 지역", example = "서울 강남구")
        private String selectedRegion;
    }
}