package tricode.eduve.dto.response;

import lombok.Getter;
import lombok.Setter;
import tricode.eduve.domain.Folder;

@Getter
@Setter
public class RootFolderDto {
    private Long folderId;
    private String folderName;

    public static RootFolderDto fromEntity(Folder folder) {
        RootFolderDto dto = new RootFolderDto();
        dto.setFolderId(folder.getFolderId());
        dto.setFolderName(folder.getName());
        return dto;
    }
}
