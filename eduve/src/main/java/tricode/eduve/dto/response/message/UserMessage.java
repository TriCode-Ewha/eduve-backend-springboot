package tricode.eduve.dto.response.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tricode.eduve.domain.Message;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserMessage{
    private final Long messageId;
    private final String question;
    private final LocalDateTime createdTime;

    public UserMessage(Message message) {
        this.messageId = message.getMessageId();
        this.question = message.getContent();
        this.createdTime = message.getCreatedTime();
    }
}
