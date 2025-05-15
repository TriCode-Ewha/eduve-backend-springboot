package tricode.eduve.dto.response.message;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tricode.eduve.domain.Message;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MessageUnitDto {
    private final UserMessage userMessage;
    private final BotMessage botMessage;
    private final List<String> fileNameAndUrl;


    public static MessageUnitDto from(Message userMessage, Message botMessage, List<String> fileNameAndUrl) {
        return new MessageUnitDto(
                new UserMessage(userMessage),
                new BotMessage(botMessage, userMessage.getMessageId()),
                fileNameAndUrl
        );
    }
}

