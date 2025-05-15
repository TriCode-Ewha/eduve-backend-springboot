package tricode.eduve.dto.response.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OneMessageDto {
    private final Long messageId;
    private final String content;
    private final boolean isUserMessage;
    private final Long questionMessageId;
    private final LocalDateTime createdTime;
    private boolean isLiked;
}
