package tricode.eduve.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tricode.eduve.dto.response.message.MessageUnitDto;
import tricode.eduve.dto.request.MessageRequestDto;
import tricode.eduve.service.ChatService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/start/{userId}")
    public ResponseEntity<MessageUnitDto> startConversation(@RequestBody MessageRequestDto requestDto,
                                                            @PathVariable(name = "userId") Long userId) throws Exception {
        return ResponseEntity.ok(chatService.startConversation(requestDto, userId));
    }
}

