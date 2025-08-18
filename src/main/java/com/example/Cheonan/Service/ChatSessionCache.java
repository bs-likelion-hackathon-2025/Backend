// src/main/java/com/example/Cheonan/Service/ChatSessionCache.java
package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.IntentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.UUID;

@Component
public class ChatSessionCache {

    @Getter
    @AllArgsConstructor
    public static class SessionData {
        private final String reply;        // 1단계에서 생성한 답변(옵션)
        private final IntentResult intent; // 2단계 추천용 필터
    }

    private final Cache<String, SessionData> cache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(5))   // TTL 5분
                    .maximumSize(100_000)                      // LRU 상한
                    .build();

    public String put(IntentResult intent, String reply) {
        String requestId = UUID.randomUUID().toString();
        cache.put(requestId, new SessionData(reply, intent));
        return requestId;
    }

    public SessionData get(String requestId) {
        return cache.getIfPresent(requestId);
    }

    public void invalidate(String requestId) {
        cache.invalidate(requestId);
    }
}