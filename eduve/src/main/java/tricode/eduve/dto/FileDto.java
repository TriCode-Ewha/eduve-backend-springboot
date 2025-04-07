package tricode.eduve.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import tricode.eduve.domain.File;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDto {
    private Long FileId;
    private String FileName;
    private String FilePath;

    public static FileDto fromEntity(File file) {
        FileDto dto = new FileDto();
        dto.setFileId(file.getFileId());
        dto.setFileName(file.getFileName());
        dto.setFilePath(file.getFullPath()); // 여기서 동적으로 경로 계산
        return dto;
    }
}