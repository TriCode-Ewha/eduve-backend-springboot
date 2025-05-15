package tricode.eduve.dto.response.file_folder;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FileUploadResponseDto {
    private List<FileResponseDto> fileInfo;
    private String flaskMessage;

    @Builder
    public FileUploadResponseDto(List<FileResponseDto> fileInfo, String flaskMessage) {
        this.fileInfo = fileInfo;
        this.flaskMessage = flaskMessage;
    }
}
