package tricode.eduve.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FileUploadResponseDto {
    private FileResponseDto fileInfo;
    private String flaskMessage;
}
