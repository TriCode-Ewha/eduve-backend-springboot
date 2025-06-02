package tricode.eduve.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tricode.eduve.domain.File;
import tricode.eduve.domain.FileType;
import tricode.eduve.domain.Folder;
import tricode.eduve.domain.User;
import tricode.eduve.dto.response.file_folder.FileResponseDto;
import tricode.eduve.dto.response.file_folder.FileUploadResponseDto;
import tricode.eduve.global.FlaskComponent;
import tricode.eduve.repository.FileRepository;
import tricode.eduve.repository.FolderRepository;
import tricode.eduve.repository.UserRepository;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FileUploadService {
    private final AmazonS3Client amazonS3Client;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final FlaskComponent flaskComponent;
    private final DagloSTTService dagloSTTService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

/*    public FileUploadResponseDto uploadAudioAndTranscribe(MultipartFile audioFile, Long userId, Long folderId) throws IOException{
        // 1. 음성 파일 S3 업로드
        FileResponseDto audioFileInfo = uploadFileToS3(audioFile, userId, folderId);

        // 2. 다글로 STT 호출
        java.io.File sttTempFile = convertMultipartToFile(audioFile);
        String transcript = String.valueOf(dagloSTTService.transcribe(sttTempFile));

        // 3. 텍스트 파일 생성
        String txtFilename = StringUtils.stripFilenameExtension(audioFile.getOriginalFilename()) + "_transcribed.txt";
        java.io.File textFile = new java.io.File(System.getProperty("java.io.tmpdir"), txtFilename);
        org.apache.commons.io.FileUtils.writeStringToFile(textFile, transcript, StandardCharsets.UTF_8);

        // 4. 텍스트 파일을 아래에 있는 일반 파일 업로드로 S3에 올리고 임베딩도 함.
        MultipartFile textMultipartFile = new MockMultipartFile(
                txtFilename,
                txtFilename,
                "text/plain",
                transcript.getBytes(StandardCharsets.UTF_8)
        );

        FileResponseDto textFileInfo = uploadFileToS3(textMultipartFile, userId, folderId);

        String flaskResult = embedDocument(textMultipartFile);

        FileUploadResponseDto responseDto = FileUploadResponseDto.builder()
                .fileInfo(List.of(audioFileInfo, textFileInfo))
                .flaskMessage(flaskResult)
                .build();
        return responseDto;

    }*/

    /**
     * 1. 음성 파일을 S3에 업로드하고
     * 2. 비동기 STT 요청을 보낸다 (rid 반환) -> 기다림
     * 3. STT 결과를 가져와 텍스트 파일 생성 후 업로드
     */
    public FileUploadResponseDto uploadAudioAndTranscribe(MultipartFile audioFile, Long userId, Long folderId) throws IOException {
        // 1. 음성 파일 S3 업로드
        FileResponseDto audioFileInfo = uploadFileToS3(audioFile, userId, folderId);

        // 2. 다글로 STT 비동기 요청
        java.io.File sttTempFile = convertMultipartToFile(audioFile);
        String fileUrl = audioFileInfo.getFileUrl(); // S3 업로드된 URL 사용
        Optional<String> optionalRid = dagloSTTService.requestTranscription(fileUrl);

        if (optionalRid.isEmpty()) {
            throw new IOException("STT 요청 실패: rid 없음");
        }

        String rid = optionalRid.get();

        // 3. STT 결과 지연 대기 & 폴링
        String transcript = null;
        int maxAttempts = 10;            // 최대 10번 시도
        int delayMillis = 3000;          // 3초 간격

        for (int i = 0; i < maxAttempts; i++) {
            Optional<String> optionalTranscript = dagloSTTService.getTranscriptionResult(rid);
            if (optionalTranscript.isPresent()) {
                transcript = optionalTranscript.get();
                break;
            }
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("STT 결과 대기 중 인터럽트 발생", e);
            }
        }

        if (transcript == null) {
            throw new IOException("STT 결과를 기다리는 동안 실패했습니다.");
        }

        // 4. 텍스트 파일 생성 및 S3 업로드
        String txtFilename = audioFileInfo.getFileName() + ".txt";

        MultipartFile textMultipartFile = new MockMultipartFile(
                txtFilename,
                txtFilename,
                "text/plain",
                transcript.getBytes(StandardCharsets.UTF_8)
        );

        FileResponseDto textFileInfo = uploadFileToS3(textMultipartFile, userId, folderId);

        // 5. Flask 임베딩 호출
        String flaskResult = embedDocument(textMultipartFile, userId);

        return FileUploadResponseDto.builder()
                .fileInfo(List.of(audioFileInfo, textFileInfo))
                .flaskMessage(flaskResult)
                .build();
    }

    public FileResponseDto uploadFileToS3(MultipartFile file, Long userId, Long folderId) throws IOException {
        // 파일 이름과 URL 생성
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IOException("파일 이름이 유효하지 않습니다.");
        }

        // 파일 확장자 추출
        String extension = StringUtils.getFilenameExtension(originalFileName);
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

        //  사용자 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IOException("User not found");
        }

        User user = userOptional.get();

        // 폴더 조회
        Folder folder = null;
        if(folderId != null) {
            Optional<Folder> folderOptional = folderRepository.findById(folderId);
            if (folderOptional.isEmpty()) {
                throw new IOException("Folder not found");
            }
            folder = folderOptional.get();
        }

        // 파일 이름 중복 검사 및 이름 결정
        String baseName = originalFileName;
        if (originalFileName.contains(".")) {
            baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        }

        String newFileName = baseName + "." + extension;
        int count = 1;
        while (fileRepository.existsByUserAndFileName(user, userId + "^" + newFileName)) {
            count++;
            newFileName = baseName + count + "." + extension;
        }

        String storedFileName = userId + "^" + newFileName;
        String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + storedFileName;

        // S3에 업로드
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(bucket, storedFileName, file.getInputStream(), metadata);

        // File 엔티티 생성 및 저장
        File fileEntity = File.builder()
                .fileName(storedFileName)  // DB에는 userId^파일명 저장
                .fileType(fileType)
                .fileUrl(fileUrl)
                .user(user)
                .folder(folder)
                .build();

        fileRepository.save(fileEntity);

        // 프론트에는 userId^ 제거해서 반환
        return FileResponseDto.from(fileEntity, userId + "^");
    }

    public String embedDocument(MultipartFile file, Long userId) throws IOException {
        return flaskComponent.embedDocument(file, userId);
    }

    private java.io.File convertMultipartToFile(MultipartFile file) throws IOException {
        java.io.File convFile = new java.io.File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }
}
