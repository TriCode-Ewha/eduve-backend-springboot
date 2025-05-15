package tricode.eduve.dto.response.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tricode.eduve.domain.Conversation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ConversationResponseDto {

    private final Long conversationId;
    private final String conversationName;
    private final LocalDateTime createdTime;


    public static List<ConversationResponseDto> from(List<Conversation> conversations) {

        List<ConversationResponseDto> dtos = new ArrayList<>();
        for(Conversation conversation : conversations) {
            dtos.add(
                    new ConversationResponseDto(
                            conversation.getConversationId(),
                            conversation.getConversationName(),
                            conversation.getCreatedTime()
                    )
            );
        }
        return dtos;
    }
}
