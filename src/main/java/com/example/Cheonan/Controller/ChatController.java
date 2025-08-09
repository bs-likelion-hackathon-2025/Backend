//package com.example.Cheonan.Controller;
//
//import com.example.Cheonan.Dto.ChatRequest;
//import com.example.Cheonan.Dto.ChatResponse;
//import com.example.Cheonan.Entity.ChatMessage;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/chat")
//@Tag(name = "챗봇 API", description = "사용자 메시지 처리 및 대화 기록 조회 API")
//public class ChatController {
//
//    private final ChatService chatService;
//
//    public ChatController(ChatService chatService) {
//        this.chatService = chatService;
//    }
//
//    @Operation(summary = "사용자 메시지 처리", description = "사용자의 메시지를 처리하고 챗봇의 응답을 반환합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "응답 성공"),
//            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류"),
//            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
//    })
//    @PostMapping
//    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
//        ChatResponse response = chatService.processMessage(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(summary = "대화 히스토리 조회", description = "특정 사용자 ID의 이전 대화 기록을 반환합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "대화 기록 반환 성공"),
//            @ApiResponse(responseCode = "400", description = "잘못된 사용자 ID"),
//            @ApiResponse(responseCode = "500", description = "서버 오류")
//    })
//    @GetMapping("/history")
//    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam String userId) {
//        List<ChatMessage> history = chatService.getChatHistory(userId);
//        return ResponseEntity.ok(history);
//    }
//}