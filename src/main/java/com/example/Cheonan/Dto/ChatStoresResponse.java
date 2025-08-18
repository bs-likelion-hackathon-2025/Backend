// src/main/java/com/example/Cheonan/Dto/StoresResponse.java
package com.example.Cheonan.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ChatStoresResponse {
    private List<ChatRecommendedStore> stores;
}