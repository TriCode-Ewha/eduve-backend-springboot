package tricode.eduve.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.File;
import tricode.eduve.domain.Folder;
import tricode.eduve.domain.User;
import tricode.eduve.dto.response.file_folder.FolderDto;
import tricode.eduve.dto.response.file_folder.RootItemDto;
import tricode.eduve.repository.FileRepository;
import tricode.eduve.repository.FolderRepository;
import tricode.eduve.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;


    public FolderDto createFolder(String name, Long parentId, Long userId) {
        User user = findUserById(userId);

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

    // 특정 폴더 하나 조회
    public FolderDto getFolder(Long folderId, Long userId, String sort) {
        Folder folder =  findFolderById(folderId);
        User user = findUserById(userId);

        // 연결된 선생님 찾기
        User teacher = null;
        if (user.getTeacherUsername() != null) {
            teacher = userRepository.findByUsernameAndRole(user.getTeacherUsername(), "ROLE_Teacher")
                    .orElseThrow(() -> new RuntimeException("선생님을 찾을 수 없습니다."));
        }
        return FolderDto.fromEntity(folder, user, teacher, sort);
    }


    // 폴더 삭제
    public void deleteFolder(Long folderId) {
        folderRepository.deleteById(folderId);
    }

    // 파일 경로 path 조회
    public String getFilePath(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
        return file.getFullPath(); // 저장된 path가 아닌 동적 계산된 path 반환
    }


    //최상위 폴더/파일 리스트 조회(선생님+자기꺼)
    public List<RootItemDto> getRootItemByUser(Long userId, String sort) {
        User user = findUserById(userId);

        // 자기 자신 + (optional) 선생님 포함할 수 있도록 변경
        List<User> accessibleUsers = new ArrayList<>();
        accessibleUsers.add(user);

        // 연결된 선생님이 있다면 추가
        if (user.getTeacherUsername() != null) {
            User teacher = userRepository.findByUsernameAndRole(user.getTeacherUsername(), "ROLE_Teacher")
                    .orElseThrow(() -> new RuntimeException("선생님을 찾을 수 없습니다."));
            accessibleUsers.add(teacher);
        }

        List<Folder> rootFolderList = folderRepository.findByUserInAndParentFolderIsNull(accessibleUsers);
        List<File> rootFileList = fileRepository.findByUserInAndFolderIsNull(accessibleUsers);

        List<Folder> sortedRootFolderList = sortFolders(rootFolderList, sort);
        List<File> sortedRootFileList = sortFiles(rootFileList, sort);

        // 루트 폴더 + 루트 파일을 RootItemDto로 변환해서 합치기
        List<RootItemDto> rootItems = new ArrayList<>();
        for (Folder folder : sortedRootFolderList) {
            rootItems.add(RootItemDto.fromEntity(folder));
        }
        for (File file : sortedRootFileList) {
            rootItems.add(RootItemDto.fromEntity(file));
        }

        return rootItems;
    }

    
    //폴더 이름 수정
    public ResponseEntity<FolderDto> updateFolder(Long folderId, String newFolderName, Long userId) {
        User user = findUserById(userId);
        Folder folder =  findFolderById(folderId);

        // 수정 권한 확인
        if(!Objects.equals(user.getUserId(), folder.getUser().getUserId())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        folder.setName(newFolderName);
        folderRepository.save(folder);
        return ResponseEntity.ok(FolderDto.fromEntity(folder));
    }

    // 파일 path 수정
    public String updateFolderPath(Long folderId, Long newParentFolderId, Long userId) {
        User user = findUserById(userId);
        Folder folder =  findFolderById(folderId);

        // 수정 권한 확인
        if(!Objects.equals(user.getUserId(), folder.getUser().getUserId())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        Folder newParentFolder =  folderRepository.findById(newParentFolderId)
                .orElseThrow(() -> new RuntimeException("새로운 상위 폴더를 찾을 수 없습니다."));

        folder.setParentFolder(newParentFolder);
        folder.updatePath();
        folderRepository.save(folder);

        return folder.getPath();
    }


    // User조회
    public User findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        return user;
    }
    // Folder조회
    public Folder findFolderById(Long folderId) {
        Folder folder =  folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없습니다."));
        return folder;
    }

    //파일 정렬
    private List<Folder> sortFolders(List<Folder> folders, String sort) {
        if (sort == null || sort.equals("default")) {
            folders.sort(Comparator.comparing(Folder::getFolderId));
        } else if (sort.equals("latest")) {
            folders.sort(Comparator.comparing(Folder::getCreatedTime).reversed());
        } else if (sort.equals("name")) {
            folders.sort(Comparator.comparing(Folder::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        }
        return folders;
    }

    //폴더 정렬
    private List<File> sortFiles(List<File> files, String sort) {
        if (sort == null || sort.equals("default")) {
            files.sort(Comparator.comparing(File::getFileId));
        } else if (sort.equals("latest")) {
            files.sort(Comparator.comparing(File::getCreatedTime).reversed());
        } else if (sort.equals("name")) {
            files.sort(Comparator.comparing(File::getFileName, Comparator.nullsLast(String::compareToIgnoreCase)));
        }
        return files;
    }
}

