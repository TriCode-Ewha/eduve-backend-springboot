package tricode.eduve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tricode.eduve.domain.Folder;
import tricode.eduve.dto.FileDto;
import tricode.eduve.dto.FolderDto;
import tricode.eduve.dto.response.RootFolderDto;
import tricode.eduve.service.FolderService;

import java.util.List;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    // 폴더 생성
    @PostMapping
    public ResponseEntity<FolderDto> createFolder(
            @RequestParam String folderName,
            @RequestParam(required = false) Long parentId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(folderService.createFolder(folderName, parentId, userId));
    }

    // 폴더 하나 조회
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderDto> getFolder(@PathVariable Long folderId) {
        return ResponseEntity.ok(folderService.getFolder(folderId));
    }

    // 최상위 폴더 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RootFolderDto>> getRootFoldersByUse(@PathVariable Long userId) {
        return ResponseEntity.ok(folderService.getRootFoldersByUser(userId));
    }

    // 폴더 삭제
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }

    // 파일 path 구하기
    @GetMapping("/files/{fileId}/path")
    public ResponseEntity<FileDto> getFilePath(@PathVariable Long fileId) {
        String path = folderService.getFilePath(fileId);
        FileDto dto = new FileDto();
        dto.setFilePath(path);
        return ResponseEntity.ok(dto);
    }
}
