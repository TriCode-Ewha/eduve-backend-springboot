package tricode.eduve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tricode.eduve.dto.response.FileResponseDto;
import tricode.eduve.global.FlaskComponent;
import tricode.eduve.service.FileService;
import tricode.eduve.service.FileUploadService;
import tricode.eduve.service.FolderService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/resources/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;
    private final FileService fileService;

    // 일반 파일 업로드
    @PostMapping("/text")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId, @RequestParam("folderId") Long folderId) {
        try {
            // 1. 파일을 S3에 업로드
            String fileUrl = fileUploadService.uploadFileToS3(file, userId,folderId);

            // 2. Flask로 파일 전달하여 임베딩 수행
            String flaskResult = fileUploadService.embedDocument(file);

            // 3. Flask 결과 + S3 URL 같이 리턴 가능
            String result = "S3 URL: " + fileUrl + "\nFlask Response: " + flaskResult;
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }

    // 파일 조회
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(@PathVariable Long fileId) {
        FileResponseDto fileResponseDto = fileService.getFileById(fileId);
        return ResponseEntity.ok(fileResponseDto);
    }

    // 파일 삭제
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    // 파일 이름 변경
    @PatchMapping("/{fileId}/rename")
    public ResponseEntity<FileResponseDto> renameFile(@PathVariable Long fileId, @RequestBody Map<String, String> body) {
        String newName = body.get("newName");
        return ResponseEntity.ok(fileService.renameFile(fileId, newName));
    }

    // 파일 위치 변경 (폴더 이동)
    @PatchMapping("/{fileId}/move")
    public ResponseEntity<FileResponseDto> moveFile(@PathVariable Long fileId, @RequestBody Map<String, Long> body) {
        Long newFolderId = body.get("newFolderId");
        return ResponseEntity.ok(fileService.moveFile(fileId, newFolderId));
    }
}