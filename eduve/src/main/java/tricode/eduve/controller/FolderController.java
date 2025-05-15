package tricode.eduve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tricode.eduve.dto.response.file_folder.FolderPathDto;
import tricode.eduve.dto.response.file_folder.FileDto;
import tricode.eduve.dto.response.file_folder.FolderDto;
import tricode.eduve.dto.response.file_folder.RootItemDto;
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
    @GetMapping("/user/{userId}/folder/{folderId}")
    public ResponseEntity<FolderDto> getFolder(@PathVariable Long folderId,
                                               @PathVariable Long userId,
                                               @RequestParam(defaultValue = "default") String sort) {
        return ResponseEntity.ok(folderService.getFolder(folderId, userId, sort));
    }

    // 최상위 폴더 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RootItemDto>> getRootItemByUse(@PathVariable Long userId,
                                                              @RequestParam(defaultValue = "default") String sort) {
        return ResponseEntity.ok(folderService.getRootItemByUser(userId, sort));
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

    // 폴더 이름 변경
    @PatchMapping("/{folderId}")
    public ResponseEntity<FolderDto> updateFolder(@PathVariable Long folderId,
                                                  @RequestParam String newFolderName,
                                                  @RequestParam Long userId) {
        return folderService.updateFolder(folderId, newFolderName, userId);
    }

    // 폴더 위치 변경 (폴더 path 수정)
    @PatchMapping("/{folderId}/path")
    public ResponseEntity<FolderPathDto> updateFilePath(@PathVariable Long folderId,
                                                        @RequestParam Long newParentFolderId,
                                                        @RequestParam Long userId){
        String path = folderService.updateFolderPath(folderId, newParentFolderId, userId);

        return ResponseEntity.ok(new FolderPathDto(path));
    }
}
