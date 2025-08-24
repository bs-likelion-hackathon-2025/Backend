package com.example.Cheonan.Dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(name = "KakaoResponse", description = "카카오 장소 검색 응답 루트")
public class KakaoResponse {

    @ArraySchema(schema = @Schema(implementation = KakaoDocument.class),
            arraySchema = @Schema(description = "장소 문서 목록"))
    private List<KakaoDocument> documents;

    @Schema(description = "응답 메타데이터")
    private KakaoMeta meta;
}