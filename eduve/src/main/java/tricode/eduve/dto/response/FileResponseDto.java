package tricode.eduve.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tricode.eduve.domain.File;

@Getter
@AllArgsConstructor
@Builder
public class FileResponseDto {
    private Long fileId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long userId;

    // File 엔티티를 DTO로 변환하는 정적 메서드
    public static FileResponseDto from(File file) {
        return FileResponseDto.builder()
                .fileId(file.getFileId())
                .fileName(file.getFileName())
                .fileType(file.getFileType().name()) // Enum -> String 변환
                .fileUrl(file.getFileUrl())
                .userId(file.getUser().getUserId()) // User 엔티티의 userId를 가져옴
                .build();
    }
}
