package tricode.eduve.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tricode.eduve.service.MessageLikeService;

@RestController
@RequestMapping("/messagelike")
@RequiredArgsConstructor
public class MessageLikeController {

    private final MessageLikeService messageLikeService;

    // 메시지 좋아요 생성
    @PostMapping("/{messageId}")
    public ResponseEntity<String> createMessageLike(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageLikeService.createMessageLike(messageId));
    }

    // 메시지 좋아요 취소
    @DeleteMapping("/{messageId}")
    public ResponseEntity<String> deleteMessageLike(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageLikeService.deleteMessageLike(messageId));
    }


    @PostMapping("/analyze/{userId}")
    public ResponseEntity<String> analyzePreference(@PathVariable Long userId) {
        messageLikeService.runAnalysis(userId);
        return ResponseEntity.ok("좋아요 기반 선호 스타일 분석 완료");
    }
}
