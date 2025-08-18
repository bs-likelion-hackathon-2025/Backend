package com.example.Cheonan.Dto;

import lombok.Builder;
import lombok.Getter;


/**
 * 모든 API 성공 / 실패를 응답 구조 통일
 */
@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ApiError error;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).error(null).build();
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return ApiResponse.<T>builder().success(false).data(null).error(error).build();
    }
}
