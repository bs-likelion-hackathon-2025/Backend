// src/main/java/com/example/Cheonan/Dto/PageResponse.java
package com.example.Cheonan.Dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {
    private List<T> items;     // 카드 배열
    private Integer size;      // 요청한 size
//    private Long nextCursor;   // 다음 페이지 커서 (마지막 id). 더 없으면 null
//    private boolean hasMore;   // 다음 페이지 존재 여부
}