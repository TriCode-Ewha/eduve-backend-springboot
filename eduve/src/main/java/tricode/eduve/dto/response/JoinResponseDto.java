package tricode.eduve.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tricode.eduve.dto.JoinDTO;

@Getter
@AllArgsConstructor
@Builder
public class JoinResponseDto {
    private JoinDTO joinDto;
    private Long userId;
}
