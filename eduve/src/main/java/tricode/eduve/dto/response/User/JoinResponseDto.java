package tricode.eduve.dto.response.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tricode.eduve.dto.request.User.JoinDTO;

@Getter
@AllArgsConstructor
@Builder
public class JoinResponseDto {
    private JoinDTO joinDto;
    private Long userId;
}
