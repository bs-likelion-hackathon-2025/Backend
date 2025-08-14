package com.example.Cheonan.Controller;

import com.example.Cheonan.Dto.ChatRecommendResponse;
import com.example.Cheonan.Dto.ChatRequest;
// import com.example.Cheonan.Dto.ChatResponse; // ❌ 사용 안 하면 제거
//import com.example.Cheonan.Entity.ChatMessage;
import com.example.Cheonan.Service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "챗봇 API", description = "사용자 메시지 처리 및 대화 기록 조회 API")
public class ChatController {

    private final ChatService chatService;

    /**
     * ✅ 의도/맥락 추출 + 가게 추천 일괄 처리
     * - 요청: ChatRequest { userId(옵션), message(비어도 서비스가 복구) }
     * - 응답: ChatRecommendResponse { reply, intent, stores[] }
     */
    @Operation(summary = "사용자 메시지 처리", description = "사용자의 메시지를 처리하고 챗봇의 응답을 반환합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "응답 성공"),
//            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류"),
//            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
//    })
    @PostMapping(
            value = "/recommend",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChatRecommendResponse> chatWithRecommendation(
            @RequestBody(required = false) ChatRequest request
    ) {
        // 요청 본문 자체가 null인 극단 케이스만 400
        if (request == null) {
            return ResponseEntity.badRequest().body(
                    ChatRecommendResponse.builder()
                            .reply("요청 본문이 비어 있어요.")
                            .intent(null)
                            .stores(List.of())
                            .build()
            );
        }
        // userId/message 공란은 서비스가 안전하게 처리함
        ChatRecommendResponse response = chatService.processMessageWithRecommendation(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅 히스토리 조회
     * - GET /chat/history?userId=xxx
     */
//    @GetMapping(
//            value = "/history",
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam String userId) {
//        if (userId == null || userId.isBlank()) {
//            return ResponseEntity.badRequest().build();
//        }
//        List<ChatMessage> history = chatService.getChatHistory(userId);
//        return ResponseEntity.ok(history);
//    }
}
