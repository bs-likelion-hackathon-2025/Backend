package com.example.Cheonan.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSelectRequest {
    @NotBlank
    private String requestId;

    @NotBlank
    private String storeName; // 사용자가 선택한 가게
}