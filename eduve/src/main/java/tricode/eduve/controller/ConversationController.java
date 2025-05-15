package tricode.eduve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tricode.eduve.dto.response.message.ConversationWithMessagesDto;
import tricode.eduve.dto.response.message.MessagesResponseDto;
import tricode.eduve.service.ConversationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    /*
    // 동기식 처리
    // 새로운 대화 시작
    @PostMapping("/start")
    public ResponseEntity<MessageUnitDto> startConversation(@RequestBody MessageRequestDto requestDto,
                                                            @RequestParam(name = "userId") Long userId) throws JsonProcessingException {
        return ResponseEntity.ok(conversationService.startConversation(requestDto, userId));
    }

    // 비동기 처리
    // 1. 질문을 받고 비동기 처리 시작
    @PostMapping("/start/async")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody MessageRequestDto requestDto) {
        return ResponseEntity.ok(conversationService.startConversationAsync(requestDto));
    }

    // 2. 질문 응답 조회 (프론트에서 폴링)
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageUnitDto> getAnswer(@PathVariable Long messageId) {
        return ResponseEntity.ok(chatService.getAnswer(messageId));
    }

    // 특정 사용자의 대화 목록 조회
    @GetMapping("user/{userId}")
    public ResponseEntity<List<ConversationResponseDto>> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }

    // 특정 대화의 메시지 목록 조회
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<MessagesResponseDto> getMessagesByConversationId(@PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getMessagesByConversationId(conversationId));
    }


    // 대화에 메시지 추가
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<Message> addMessage(@PathVariable Long conversationId, @RequestParam String content) {
        return ResponseEntity.ok(conversationService.addMessage(conversationId, content));
    }

     */

    // 특정 사용자의 모든 대화 및 메시지 목록 조회
    @GetMapping("/user/{userId}")
    public List<ConversationWithMessagesDto> getConversationsByUser(@PathVariable Long userId) {
        return conversationService.getConversationsWithMessagesByUserId(userId);
    }

    // 특정 Conversation의 메시지 목록 조회
    @GetMapping("/{conversationId}/messages")
    public MessagesResponseDto getMessagesByConversation(@PathVariable Long conversationId) {
        return conversationService.getMessagesByConversationId(conversationId);
    }
}
