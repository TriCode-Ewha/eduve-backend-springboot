package tricode.eduve.dto.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileInfoDto {
    private String fileName;
    private String page;
    private String fileUrl;
    private String filePath;

    public FileInfoDto(String fileName, String page, String fileUrl, String filePath) {
        this.fileName = fileName;
        this.page = page;
        this.fileUrl = fileUrl;
        this.filePath = filePath;
    }
}
