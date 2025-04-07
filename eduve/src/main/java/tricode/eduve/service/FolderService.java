package tricode.eduve.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.File;
import tricode.eduve.domain.Folder;
import tricode.eduve.domain.User;
import tricode.eduve.dto.FolderDto;
import tricode.eduve.dto.response.RootFolderDto;
import tricode.eduve.repository.FileRepository;
import tricode.eduve.repository.FolderRepository;
import tricode.eduve.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;


    public FolderDto createFolder(String name, Long parentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Folder folder = new Folder(name, user);

        if (parentId != null) {
            Folder parent = folderRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("부모 폴더를 찾을 수 없습니다."));
            folder.setParentFolder(parent);
        } else {
            folder.setParentFolder(null); // 최상위 폴더
        }

        folder.updatePath();
        return FolderDto.fromEntity(folderRepository.save(folder));
    }

    public FolderDto getFolder(Long folderId) {
        Folder folder =  folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없습니다."));
        return FolderDto.fromEntity(folder);
    }

    public List<FolderDto> getAllFolders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return folderRepository.findAll().stream()
                .map(FolderDto::fromEntity)
                .collect(Collectors.toList());
    }

    public void deleteFolder(Long folderId) {
        folderRepository.deleteById(folderId);
    }

    public String getFilePath(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
        return file.getFullPath(); // 저장된 path가 아닌 동적 계산된 path 반환
    }

    public List<RootFolderDto> getRootFoldersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return folderRepository.findByUserAndParentFolderIsNull(user).stream()
                .map(RootFolderDto::fromEntity)
                .collect(Collectors.toList());
    }
}

