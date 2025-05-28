package tricode.eduve.dto.response.message;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tricode.eduve.domain.Message;
import tricode.eduve.dto.common.FileInfoDto;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MessageUnitDto {
    private final UserMessage userMessage;
    private final BotMessage botMessage;
    private final FileInfoDto fileInfo;


    public static MessageUnitDto from(Message userMessage, Message botMessage, FileInfoDto fileInfo) {
        return new MessageUnitDto(
                new UserMessage(userMessage),
                new BotMessage(botMessage, userMessage.getMessageId()),
                fileInfo
        );
    }
}

