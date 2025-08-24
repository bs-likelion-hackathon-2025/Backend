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

    /** 1단계: 메시지 → 히스토리에 추가 + AI 응답 생성 */
    public ChatReplyResponse handleMessage(ChatRequest req) {
        var session = (req.getRequestId() != null)
                ? sessionCache.get(req.getRequestId())
                : null;

        if (session == null) {
            session = sessionCache.createSession();
        }

        session.addUserMessage(req.getMessage());

        var ai = geminiClient.getFoodRecommendationWithIntent(session.getAllMessagesAsPrompt());

        String reply = (ai != null && ai.getReply() != null) ? ai.getReply() : "요청을 처리하지 못했어요.";
        IntentResult intent = (ai != null) ? ai.getIntent() : null;

        session.addBotMessage(reply);
        session.setIntent(intent);
        sessionCache.put(session);

        return ChatReplyResponse.builder()
                .requestId(session.getRequestId())
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
                session.getLastUserMessage(), session.getIntent());

        var storeDtos = stores.stream()
                .map(ChatRecommendedStore::fromEntity)
                .toList();

        return ChatStoresResponse.builder().stores(storeDtos).build();
    }

    /** 3단계: 선택한 가게 처리 */
    public ChatReplyResponse handleStoreSelection(String requestId, String storeName) {
        var session = sessionCache.get(requestId);
        if (session == null) {
            throw new NoContentException("잘못되거나 만료된 requestId");
        }

        // 추천 목록에서 유효성 검증
        var recommendedStores = recommendService.recommendByMultiIntentOrFallback(
                session.getLastUserMessage(), session.getIntent());

        boolean isValid = recommendedStores.stream()
                .anyMatch(store -> store.getName().equals(storeName));
        if (!isValid) {
            return ChatReplyResponse.builder()
                    .requestId(requestId)
                    .reply("죄송합니다. 선택하신 가게는 추천 목록에 없습니다.")
                    .build();
        }

        session.addUserMessage(storeName);
        sessionCache.put(session);

        String reply = storeName + "으로 선택하셨습니다! 즐거운 식사 되시길 바랍니다.";
        session.addBotMessage(reply);

        return ChatReplyResponse.builder()
                .requestId(requestId)
                .reply(reply)
                .build();
    }
}