// src/main/java/com/example/Cheonan/Service/ChatTwoStepService.java
package com.example.Cheonan.Service;

import com.example.Cheonan.Dto.*;
import com.example.Cheonan.Exception.NoContentException;
import com.example.Cheonan.Util.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatTwoStepService {

    private final GeminiClient geminiClient;
    private final StoreRecommendService recommendService;
    private final ChatSessionCache sessionCache;

    public ChatReplyResponse handleMessage(ChatRequest req) {
        var ai = geminiClient.getFoodRecommendationWithIntent(req.getMessage());
        String reply = (ai != null && ai.getReply() != null) ? ai.getReply() : "요청을 처리하지 못했어요.";
        IntentResult intent = (ai != null) ? ai.getIntent() : null;

        String requestId = sessionCache.put(intent, reply);

        return ChatReplyResponse.builder()
                .requestId(requestId)
                .reply(reply)
                .build();
    }

    /** intent가 비어있는지 판단 */
    private boolean hasStoreIntent(IntentResult intent) {
        if (intent == null) return false;
        boolean c1 = intent.getCategory1() != null && !intent.getCategory1().isBlank();
        boolean c2 = intent.getCategory2Candidates() != null && !intent.getCategory2Candidates().isEmpty();
        boolean c3 = intent.getCategory3Candidates() != null && !intent.getCategory3Candidates().isEmpty();
        boolean c4 = intent.getCategory4Candidates() != null && !intent.getCategory4Candidates().isEmpty();
        return c1 || c2 || c3 || c4;
    }

    /** 2단계: requestId로 intent 찾아 추천 계산 → 가게 목록 */
    public ChatStoresResponse getStores(String requestId) {
        var session = sessionCache.get(requestId);
        if (session == null) {
            throw new NoContentException("잘못되거나 만료된 requestId");
        }
        if (!hasStoreIntent(session.getIntent())) {
            throw new NoContentException("음식 관련 intent 없음");
        }

        var stores = recommendService.recommendByMultiIntentOrFallback(
                session.getReply(), session.getIntent());

        var storeDtos = stores.stream()
                .map(ChatRecommendedStore::fromEntity)
                .toList();

        return ChatStoresResponse.builder().stores(storeDtos).build();
    }
}