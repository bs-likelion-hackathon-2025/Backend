package com.example.Cheonan.Dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError {
    private String code;
    private String message;
    private String detail;
}
