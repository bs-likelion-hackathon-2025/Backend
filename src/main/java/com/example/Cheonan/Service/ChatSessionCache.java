// src/main/java/com/example/Cheonan/Service/ChatSessionCache.java
package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.IntentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ChatSessionCache {

    @Getter
    @AllArgsConstructor
    public static class SessionData {
        private final String requestId;
        private final List<String> userMessages;
        private final List<String> botMessages;
        private IntentResult intent;

        public void addUserMessage(String msg) {
            userMessages.add(msg);
        }

        public void addBotMessage(String msg) {
            botMessages.add(msg);
        }

        public String getLastUserMessage() {
            return userMessages.isEmpty() ? null : userMessages.get(userMessages.size() - 1);
        }

        public void setIntent(IntentResult intent) {
            this.intent = intent;
        }

        /** 프롬프트용 대화 전체 히스토리 반환 */
        public String getAllMessagesAsPrompt() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < userMessages.size(); i++) {
                sb.append("사용자: ").append(userMessages.get(i)).append("\n");
                if (i < botMessages.size()) {
                    sb.append("AI: ").append(botMessages.get(i)).append("\n");
                }
            }
            return sb.toString();
        }
    }

    private final Cache<String, SessionData> cache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(30))   // TTL 늘리기 (예: 30분)
                    .maximumSize(100_000)
                    .build();

    /** 새로운 세션 생성 */
    public SessionData createSession() {
        String requestId = UUID.randomUUID().toString();
        SessionData session = new SessionData(requestId, new ArrayList<>(), new ArrayList<>(), null);
        cache.put(requestId, session);
        return session;
    }

    /** 기존 세션 가져오기 */
    public SessionData get(String requestId) {
        return cache.getIfPresent(requestId);
    }

    /** 세션 저장 (갱신) */
    public void put(SessionData session) {
        cache.put(session.getRequestId(), session);
    }

    public void invalidate(String requestId) {
        cache.invalidate(requestId);
    }
}