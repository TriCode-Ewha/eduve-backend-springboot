package tricode.eduve.dto.response.file_folder;

import lombok.Getter;
import lombok.Setter;
import tricode.eduve.domain.File;
import tricode.eduve.domain.Folder;
import tricode.eduve.domain.User;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
public class FolderDto {
    private Long FolderId;
    private String FolderName;
    private String FolderOwner;
    private String path;
    private Long parentId;
    private List<FolderDto> subFolders;
    private List<FileDto> files;

    public static FolderDto fromEntity(Folder folder, User user, User teacher, String sort) {
        FolderDto dto = new FolderDto();
        dto.setFolderId(folder.getFolderId());
        dto.setFolderName(folder.getName());
        dto.setFolderOwner(folder.getUser().getName());
        dto.setPath(folder.getPath());
        dto.setParentId(folder.getParentFolder() != null ? folder.getParentFolder().getFolderId() : null);


        // 유저 또는 선생님에 해당하는 하위 폴더만 포함
        // 하위 폴더 필터링 + 정렬
        List<Folder> subFolders = folder.getSubFolders().stream()
                .filter(subFolder -> {
                    User folderOwner = subFolder.getUser();
                    return Objects.equals(folderOwner, user) || Objects.equals(folderOwner, teacher);
                })
                .collect(Collectors.toList());
        sortFolders(subFolders, sort);
        dto.setSubFolders(subFolders.stream()
                .map(subFolder -> fromEntity(subFolder, user, teacher, sort)) // 재귀적으로 정렬 포함
                .collect(Collectors.toList()));

        // 유저 또는 선생님에 해당하는 하위 파일만 포함
        // 하위 파일 필터링 + 정렬
        List<File> files = folder.getFiles().stream()
                .filter(file -> {
                    User fileOwner = file.getUser();
                    return Objects.equals(fileOwner, user) || Objects.equals(fileOwner, teacher);
                })
                .collect(Collectors.toList());
        sortFiles(files, sort);
        dto.setFiles(files.stream().map(FileDto::fromEntity).collect(Collectors.toList()));

        return dto;
    }


    public static FolderDto fromEntity(Folder folder) {
        FolderDto dto = new FolderDto();
        dto.setFolderId(folder.getFolderId());
        dto.setFolderName(folder.getName());
        dto.setFolderOwner(folder.getUser().getName());
        dto.setPath(folder.getPath());
        dto.setParentId(folder.getParentFolder() != null ? folder.getParentFolder().getFolderId() : null);
        return dto;
    }


    private static void sortFolders(List<Folder> folders, String sort) {
        if (sort == null || sort.equals("default")) {
            folders.sort(Comparator.comparing(Folder::getFolderId));
        } else if (sort.equals("latest")) {
            folders.sort(Comparator.comparing(Folder::getCreatedTime).reversed());
        } else if (sort.equals("name")) {
            folders.sort(Comparator.comparing(Folder::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        }
    }

    private static void sortFiles(List<File> files, String sort) {
        if (sort == null || sort.equals("default")) {
            files.sort(Comparator.comparing(File::getFileId));
        } else if (sort.equals("latest")) {
            files.sort(Comparator.comparing(File::getCreatedTime).reversed());
        } else if (sort.equals("name")) {
            files.sort(Comparator.comparing(File::getFileName, Comparator.nullsLast(String::compareToIgnoreCase)));
        }
    }
}
