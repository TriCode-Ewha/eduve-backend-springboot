package tricode.eduve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tricode.eduve.dto.response.file_folder.FileResponseDto;
import tricode.eduve.dto.response.file_folder.FileUploadResponseDto;
import tricode.eduve.service.FileService;
import tricode.eduve.service.FileUploadService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;
    private final FileService fileService;

    // 음성 파일 업로드
    @PostMapping("/voice")
    public ResponseEntity<FileUploadResponseDto> uploadVoiceFile(@RequestParam("file") MultipartFile file,
                                                                 @RequestParam("userId") Long userId,
                                                                 @RequestParam(value = "folderId", required = false) Long folderId) throws IOException {
        try {
            FileUploadResponseDto responseDto = fileUploadService.uploadAudioAndTranscribe(file, userId, folderId);

            return ResponseEntity.ok(responseDto);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    // 일반 파일 업로드
    @PostMapping("/text")
    public ResponseEntity<FileUploadResponseDto> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId, @RequestParam(value = "folderId", required = false) Long folderId) {
        try {
            // 1. 파일을 S3에 업로드
            FileResponseDto fileDto = fileUploadService.uploadFileToS3(file, userId, folderId);

            // 2. Flask로 파일 전달하여 임베딩 수행
            String flaskResult = fileUploadService.embedDocument(file, userId);

            // 3. 결과 합쳐서 JSON으로 반환
            FileUploadResponseDto responseDto = FileUploadResponseDto.builder()
                    .fileInfo(List.of(fileDto))
                    .flaskMessage(flaskResult)
                    .build();

            return ResponseEntity.ok(responseDto);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 파일 조회
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(@PathVariable Long fileId) {
        FileResponseDto fileResponseDto = fileService.getFileById(fileId);
        return ResponseEntity.ok(fileResponseDto);
    }

    /*
    // 파일 최신순 정렬
    @GetMapping("/sort/date")
    public ResponseEntity<List<FileResponseDto>> getFilesSortedByDate() {
        return ResponseEntity.ok(fileService.getFilesOrderedByDate());
    }

    // 파일 이름순 정렬
    @GetMapping("/sort/name")
    public ResponseEntity<List<FileResponseDto>> getFilesSortedByName() {
        return ResponseEntity.ok(fileService.getFilesOrderedByName());
    }
    */

    // 파일 이름명 검색
    @GetMapping("/search")
    public ResponseEntity<List<FileResponseDto>> searchFiles(@RequestParam String keyword) {
        return ResponseEntity.ok(fileService.searchFilesByName(keyword));
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