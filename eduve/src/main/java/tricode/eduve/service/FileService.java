package tricode.eduve.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.File;
import tricode.eduve.domain.Folder;
import tricode.eduve.dto.response.file_folder.FileResponseDto;
import tricode.eduve.repository.FileRepository;
import tricode.eduve.repository.FolderRepository;

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

        // Dto 변환 후 반환
        return FileResponseDto.from(file);
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
        return fileRepository.findByFileNameContainingIgnoreCase(keyword)
                .stream()
                .map(FileResponseDto::from)
                .collect(Collectors.toList());
    }


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
        file.setFileName(newName);
        return FileResponseDto.from(file);
    }

    // 파일 위치 변경 (폴더 이동)
    @Transactional
    public FileResponseDto moveFile(Long fileId, Long newFolderId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("file not found"));

        Folder newFolder = folderRepository.findById(newFolderId)
                .orElseThrow(() -> new IllegalArgumentException("folder not found"));

        file.setFolder(newFolder);
        return FileResponseDto.from(file);
    }
}
