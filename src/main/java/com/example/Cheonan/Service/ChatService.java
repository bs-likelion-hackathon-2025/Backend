//package com.example.Cheonan.Service;
//
//import com.example.Cheonan.Dto.ChatRecommendResponse;
//import com.example.Cheonan.Dto.ChatRequest;
//import com.example.Cheonan.Dto.ChatRecommendedStore;
////import com.example.Cheonan.Entity.ChatMessage;
////import com.example.Cheonan.Repository.ChatMessageRepository;
//import com.example.Cheonan.Util.GeminiClient;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Collections;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ChatService {
//
////    private final ChatMessageRepository chatMessageRepository;
//    private final GeminiClient geminiClient;
//    private final StoreRecommendService storeRecommendService;
//
//    /**
//     * 채팅 + 의도추출 + 추천을 한 번에 처리
//     * 1) Gemini 호출(1회) → reply + intent(JSON)
//     * 2) intent(category 등)로 가게 추천(없으면 reply 키워드 fallback)
//     * 3) 대화 로그 저장
//     * 4) reply + 추천 리스트를 함께 반환
//     */
//    @Transactional // 저장까지 포함하므로 트랜잭션
//    public ChatRecommendResponse processMessageWithRecommendation(ChatRequest request) {
//        // 0) 입력값 방어
////        final String userId = request.getUserId() == null ? "anonymous" : request.getUserId().trim();
//        final String userInput = request.getMessage() == null ? "" : request.getMessage().trim();
//
//        if (userInput.isBlank()) {
//            return ChatRecommendResponse.builder()
//                    .reply("뭐라고 할지 다시 한 번만 말씀해 주실래유? 😊")
//                    .intent(null)
//                    .stores(Collections.emptyList())
//                    .build();
//        }
//
//        // 1) Gemini 호출 (reply + intent 동시 수신)
//        ChatRecommendResponse ai = geminiClient.getFoodRecommendationWithIntent(userInput);
//        if (ai == null) {
//            ai = ChatRecommendResponse.builder()
//                    .reply("요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.")
//                    .intent(null)
//                    .stores(Collections.emptyList())
//                    .build();
//        }
//
//        // 2) intent 기반 추천 (없으면 reply 키워드 기반 fallback)
//        var stores = storeRecommendService.recommendByMultiIntentOrFallback(
//                ai.getReply(),
//                ai.getIntent()
//        );
//
//        // 3) 엔티티 → 전송용 DTO
//        var storeDtos = stores.stream()
//                .map(s -> new ChatRecommendedStore(
//                        s.getName(),
//                        s.getAddress(),
//                        s.getCategory1(),
//                        s.getCategory2(),
//                        s.getGoogleLink(),
//                        s.getKakaoLink(),
//                        s.getPhoneNumber(),
//                        s.getRating()
//                ))
//                .toList();
//
//        // 4) 채팅 로그 저장 (실패해도 추천 응답은 유지하려면 try-catch로 감싸도 OK)
////        chatMessageRepository.save(new ChatMessage(userInput, ai.getReply()));
//
//        // 5) 최종 응답(불변 DTO로 재조립)
//        return ChatRecommendResponse.builder()
//                .reply(ai.getReply())
//                .intent(ai.getIntent())
//                .stores(storeDtos)
//                .build();
//    }
//
//}