package tricode.eduve.dto.response.file_folder;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import tricode.eduve.domain.File;
import tricode.eduve.domain.Folder;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RootItemDto {
    private String type;
    private Long folderId;
    private String folderName;
    private Long fileId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private String owner;
    private LocalDateTime createdTime;

    public static RootItemDto fromEntity(Folder folder) {
        RootItemDto dto = new RootItemDto();
        dto.setType("folder");
        dto.setFolderId(folder.getFolderId());
        dto.setFolderName(folder.getName());
        dto.setOwner(folder.getUser().getUsername());
        dto.setCreatedTime(folder.getCreatedTime());
        return dto;
    }

    public static RootItemDto fromEntity(File file) {
        RootItemDto dto = new RootItemDto();
        dto.setType("file");
        dto.setFileId(file.getFileId());
        dto.setFileName(file.getFileName());
        dto.setFileUrl(file.getFileUrl());
        dto.setFileType(String.valueOf(file.getFileType()));
        dto.setOwner(file.getUser().getUsername());
        dto.setCreatedTime(file.getCreatedTime());
        return dto;
    }
}
