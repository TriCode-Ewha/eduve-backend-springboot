package tricode.eduve.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tricode.eduve.domain.File;
import tricode.eduve.domain.FileType;
import tricode.eduve.domain.User;
import tricode.eduve.repository.FileRepository;
import tricode.eduve.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FileUploadService {
    private final AmazonS3Client amazonS3Client;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public String uploadFileToS3(MultipartFile file, Long userId) throws IOException {
        // 파일 이름과 URL 생성
        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new IOException("파일 이름이 유효하지 않습니다.");
        }

        // 파일 확장자 추출
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null) {
            throw new IOException("파일 확장자를 확인할 수 없습니다.");
        }

        // 확장자에 따라 FileType 매핑
        FileType fileType = switch (extension.toLowerCase()) {
            case "pdf" -> FileType.PDF;
            case "doc", "docx" -> FileType.DOCX;
            case "hwp" -> FileType.HWP;
            case "txt" -> FileType.TEXT;
            case "ppt", "pptx" -> FileType.PPT;
            case "mp3", "wav", "m4a" -> FileType.VOICE;
            default -> throw new IOException("지원하지 않는 파일 형식입니다: " + extension);
        };

        String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;

        // S3에 파일 업로드
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

        //  사용자 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IOException("User not found");
        }

        User user = userOptional.get();

        // File 엔티티 객체 생성
        File fileEntity = File.builder()
                .fileName(fileName)
                .fileType(fileType)  // 파일 타입 설정
                .fileUrl(fileUrl)
                .user(user)
                .build();

        // 파일 엔티티를 DB에 저장
        fileRepository.save(fileEntity);

        return fileUrl; // 성공적으로 파일 URL 반환
    }
}
