package tricode.eduve.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.Message;
import tricode.eduve.domain.MessageLike;
import tricode.eduve.domain.User;
import tricode.eduve.repository.MessageLikeRepository;
import tricode.eduve.repository.MessageRepository;
import tricode.eduve.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageLikeService {

    private final MessageLikeRepository messageLikeRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageLikePreferenceService messageLikePreferenceService;

    // 메시지 좋아요 생성
    public String createMessageLike(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new RuntimeException("Message not found."));

        if(message.getQuestionMessage()==null){
            throw new RuntimeException("Message is user message");
        }

        boolean isLiked = messageLikeRepository.existsByMessage(message);

        // 메시지 좋아요가 이미 있으면 생성하지 않음
        if(isLiked) {
            throw new RuntimeException("Message like already exists.");
        }
        else{
            MessageLike messageLike = new MessageLike(message);
            messageLikeRepository.save(messageLike);

            // 해당 사용자가 좋아요를 누른 댓글 수 추적
            long likedMessagesCount = messageLikeRepository.countByUser(message.getConversation().getUser());

            // 좋아요 수가 5의 배수일 때만 분석 수행
            if (likedMessagesCount % 5 == 0) {
                runAnalysis(message.getConversation().getUser().getUserId());
            }

            return "Message Like created successfully";
        }
    }


    // 메시지 좋아요 삭제
    public String deleteMessageLike(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new RuntimeException("Message not found."));

        if(message.getQuestionMessage()==null){
            throw new RuntimeException("Message is user message");
        }

        MessageLike messageLike = messageLikeRepository.findByMessage(message)
                .orElseThrow(()-> new RuntimeException("MessageLike not found."));

        message.setLike(null);
        messageLike.setMessage(null);

        messageLikeRepository.delete(messageLike);
        return "Message Like deleted";
    }

    public void runAnalysis(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        messageLikePreferenceService.runAnalysis(user); // 기존 메서드 호출
    }
}
