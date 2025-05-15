package tricode.eduve.dto.response.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tricode.eduve.domain.Message;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class BotMessage {
    private final Long messageId;
    private final String answer;
    private final LocalDateTime createdTime;
    private final Long questionMessageId;

    public BotMessage(Message message, Long questionMessageId) {
        this.messageId = message.getMessageId();
        this.answer = message.getContent();
        this.createdTime = message.getCreatedTime();
        this.questionMessageId = questionMessageId;
    }
}
