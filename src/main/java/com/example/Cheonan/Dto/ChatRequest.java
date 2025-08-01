package com.example.Cheonan.Dto;

import lombok.Getter;

@Getter
public class ChatRequest {
    private String userId;
    private String message;

    public ChatRequest() {}

}
