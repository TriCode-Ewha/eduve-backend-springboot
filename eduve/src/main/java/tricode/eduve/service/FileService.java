package tricode.eduve.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.File;
import tricode.eduve.domain.Folder;
import tricode.eduve.dto.response.file_folder.FileResponseDto;
import tricode.eduve.repository.FileRepository;
import tricode.eduve.repository.FolderRepository;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    // 파일 하나 조회
    @Transactional
    public FileResponseDto getFileById(Long fileId) {
        // 파일 조회 (없으면 예외 발생)
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        // 접두어 (userId^)
        String prefix = file.getUser().getUserId() + "^";

        // Dto 변환 후 반환
        return FileResponseDto.from(file, prefix);
    }

    /*
    // 이름순 정렬
    @Transactional
    public List<FileResponseDto> getFilesOrderedByName() {
        return fileRepository.findAllByOrderByFileNameAsc()
                .stream()
                .map(FileResponseDto::from)
                .collect(Collectors.toList());
    }

    // 최신순 정렬
    @Transactional
    public List<FileResponseDto> getFilesOrderedByDate() {
        return fileRepository.findAllByOrderByCreatedTimeDesc()
                .stream()
                .map(FileResponseDto::from)
                .collect(Collectors.toList());
    }
    */

    // 파일 이름 검색
    @Transactional
    public List<FileResponseDto> searchFilesByName(String keyword) {
        List<File> allFiles = fileRepository.findAll();

        return allFiles.stream()
                .filter(file -> {
                    String prefix = file.getUser().getUserId() + "^";
                    String cleanFileName = file.getFileName().startsWith(prefix)
                            ? file.getFileName().substring(prefix.length())
                            : file.getFileName();
                    return cleanFileName.toLowerCase().contains(keyword.toLowerCase());
                })
                .map(file -> {
                    String prefix = file.getUser().getUserId() + "^";
                    return FileResponseDto.from(file, prefix);
                })
                .collect(Collectors.toList());
    }

//    // 파일 이름 검색
//    @Transactional
//    public List<FileResponseDto> searchFilesByName(Long userId, String keyword) {
//        List<File> userFiles = fileRepository.findByUserUserId(userId); // 사용자 별로 제한
//
//        return userFiles.stream()
//                .filter(file -> {
//                    String prefix = userId + "^";
//                    String cleanFileName = file.getFileName().startsWith(prefix)
//                            ? file.getFileName().substring(prefix.length())
//                            : file.getFileName();
//                    return cleanFileName.toLowerCase().contains(keyword.toLowerCase());
//                })
//                .map(file -> FileResponseDto.from(file, userId + "^"))
//                .collect(Collectors.toList());
//    }



    // 파일 삭제
    @Transactional
    public void deleteFile(Long fileId) {
        fileRepository.deleteById(fileId);
    }

    // 파일 이름 변경
    @Transactional
    public FileResponseDto renameFile(Long fileId, String newName) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("file not found"));

        Long userId = file.getUser().getUserId();
        String extension = StringUtils.getFilenameExtension(newName);
        if (extension == null) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }

        String baseName = newName;
        if (newName.contains(".")) {
            baseName = newName.substring(0, newName.lastIndexOf("."));
        }

        // 접두어
        String prefix = userId + "^";
        String uniqueFileName = baseName + "." + extension;
        String finalFileName = prefix + uniqueFileName;
        int counter = 2;

        while (fileRepository.existsByUserUserIdAndFileName(userId, finalFileName)) {
            uniqueFileName = baseName + counter + "." + extension;
            finalFileName = prefix + uniqueFileName;
            counter++;
        }

        file.setFileName(finalFileName);

        return FileResponseDto.from(file, prefix);  // 접두어 제거 후 반환
    }

    // 파일 위치 변경 (폴더 이동)
    @Transactional
    public FileResponseDto moveFile(Long fileId, Long newFolderId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("file not found"));

        Folder newFolder = folderRepository.findById(newFolderId)
                .orElseThrow(() -> new IllegalArgumentException("folder not found"));

        file.setFolder(newFolder);

        String prefix = file.getUser().getUserId() + "^";
        return FileResponseDto.from(file, prefix);
    }
}
