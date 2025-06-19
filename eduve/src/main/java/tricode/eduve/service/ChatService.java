package tricode.eduve.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.*;
import tricode.eduve.dto.common.FileInfoDto;
import tricode.eduve.dto.request.MessageRequestDto;
import tricode.eduve.dto.response.message.MessageUnitDto;
import tricode.eduve.global.ChatGptClient;
import tricode.eduve.global.FlaskComponent;
import tricode.eduve.repository.FileRepository;
import tricode.eduve.repository.MessageLikePreferenceRepository;
import tricode.eduve.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {

    private final ChatGptClient chatGptClient;
    private final FlaskComponent flaskComponent;
    private final ConversationService conversationService;
    private final UserRepository userRepository;
    private final UserCharacterService userCharacterService;
    private final MessageLikePreferenceRepository messageLikePreferenceRepository;
    private final FileRepository fileRepository;
    /*
    // 질문을 저장하고 비동기적으로 ChatGPT API 호출
    @Async
    public Long processQuestionAsync(MessageRequestDto requestDto) {
        // 임시 conversation 수정필요
        Conversation conversation = new Conversation();


        // 질문 저장
        Message message = messageService.save(requestDto, conversation);

        // ChatGPT API 호출을 비동기로 실행
        CompletableFuture.runAsync(() -> {
            // 유사도 검색
            String similarDocuments = flaskComponent.findSimilarDocuments(requestDto.getQuestion());
            // 유사도 검색결과와 사용자 질문을 함께 chatGPT로 보냄
            ResponseEntity<String> response= chatGptClient.chat(message.getQuestion(), similarDocuments);
            String parsedResponse = null;
            try {
                parsedResponse = parseResponse(response);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            message.setAnswer(parsedResponse);
            message.setStatus(Message.Status.COMPLETED);
        });

        return message.getMessageId();
    }

    // 메시지 응답 조회(비동기)
    public MessageUnitDto getAnswer(Long messageId) {
        Message message = messageService.findById(messageId);
        return MessageUnitDto.from(message);
    }


    // 메시지 동기 처리
    public Message processQuestion(MessageRequestDto requestDto, String similarDocuments, Conversation conversation) throws JsonProcessingException {
        Message message = messageService.save(requestDto, conversation);

        ResponseEntity<String> response= chatGptClient.chat(message.getQuestion(), similarDocuments);
        String parsedResponse = parseResponse(response);

        message.setAnswer(parsedResponse);
        message.setStatus(Message.Status.COMPLETED);

        return message;
    }

     */

    public String parseResponse(ResponseEntity<String> response) throws JsonProcessingException {
        String responseBody = response.getBody();  // 응답 본문 받기
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);

        // 'choices' 배열의 첫 번째 항목에서 'message' -> 'content' 추출
        String answer = rootNode.path("choices").get(0).path("message").path("content").asText();
        return answer;
    }


    public MessageUnitDto startConversation(MessageRequestDto requestDto, Long userId, Long graph, Long url) throws Exception {
        try {

            log.debug("📩 요청 수신 - userId: {}, graph: {}, url: {}, question: {}", userId, graph, url, requestDto.getQuestion());
            String userMessage = requestDto.getQuestion();

            log.debug("🧠 사용자 메시지 처리 시작");

            // 1. Conversation 처리 (주제 유사도검색 + 1시간 기준)
            Message message = conversationService.processUserMessage(userId, userMessage);
            log.debug("✅ 메시지 저장 완료 - messageId: {}", message.getMessageId());

            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
            log.debug("✅ 유저 조회 성공 - username: {}", user.getUsername());

            String similarDocuments = null;
            if (user.getRole().equals("ROLE_Student")) {
                log.debug("🎓 학생 유저 - 선생님 정보 조회 시도");
                Optional<User> teacher = userRepository.findByUsernameAndRole(user.getTeacherUsername(), "ROLE_Teacher");
                if (teacher.isPresent()) {
                    log.debug("✅ 선생님 찾음 - teacher: {}", teacher.get().getUsername());
                    similarDocuments = flaskComponent.findSimilarDocuments(userMessage, userId, teacher.get());
                } else {
                    log.warn("⚠️ 선생님 못 찾음 - fallback to no teacher");
                    similarDocuments = flaskComponent.findSimilarDocuments(userMessage, userId, null);
                }
            } else {
                log.debug("👨‍🏫 교직원 or 기타 유저 - 유사 문서 검색 진행");
                similarDocuments = flaskComponent.findSimilarDocuments(userMessage, userId, null);
            }
            log.debug("📄 유사 문서 검색 완료");

            FileInfoDto fileInfo = null;
            if (url == 1L) {
                log.debug("📎 파일 URL 요청 포함 - 파일 정보 추출 시작");
                fileInfo = extractFirstFileInfo(similarDocuments);
                log.debug("✅ 파일 정보 추출 완료: {}", fileInfo != null ? fileInfo.getFileName() : "없음");
            }

            log.debug("🎯 사용자 캐릭터 선호도 조회 시작");
            Preference userPreference = userCharacterService.getPrefernceByUserId(userId);
            log.debug("✅ 선호도 조회 성공: tone={}", userPreference.getTone());

            String analysisResult = messageLikePreferenceRepository.findByUser(user)
                    .map(MessageLikePreference::getAnalysisResult)
                    .orElse(null);
            log.debug("💬 좋아요 분석 결과: {}", analysisResult != null ? "있음" : "없음");

            log.debug("🤖 ChatGPT 호출 시작 - graph={}", graph);
            ResponseEntity<String> response;
            if (analysisResult != null && !analysisResult.isBlank()) {
                response = chatGptClient.chat(userMessage, similarDocuments, userPreference, analysisResult, fileInfo, graph);
            } else {
                response = chatGptClient.chat(userMessage, similarDocuments, userPreference, null, fileInfo, graph);
            }
            log.debug("✅ ChatGPT 응답 수신 - status: {}, body: {}", response.getStatusCode(), response.getBody());


            String parsedResponse = (graph == 0)
                    ? parseResponse(response)
                    : String.valueOf(response);

            Message botMessage = Message.createBotResponse(message.getConversation(), parsedResponse, message);
            conversationService.saveBotMessage(botMessage);
            log.debug("✅ 봇 메시지 저장 완료");

            return MessageUnitDto.from(message, botMessage, fileInfo);

        } catch (Exception e) {
            log.error("❌ startConversation 실패 - 요청 유저 ID: {}, 질문: {}", userId, requestDto.getQuestion(), e);
            throw new RuntimeException("대화 시작 중 오류 발생", e);
        }
    }

    // 유사도 검색 결과에서 파일 제목과 url 추출
    public FileInfoDto extractFirstFileInfo(String similarDocuments) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(similarDocuments);
        JsonNode results = root.path("results");

        // results가 배열이 아니거나 비어 있으면 null 반환
        if (!results.isArray() || results.isEmpty()) {
            log.debug("❌ results가 비어 있음");
            return null;
        }

        JsonNode firstResult = results.get(0);
        if (firstResult == null || firstResult.isEmpty()) {
            log.debug("❌ firstResult가 비어 있음");
            return null;
        }

        String fileName = firstResult.path("file_name").asText();
        log.debug("📄 fileName: {}", fileName);
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        String page = firstResult.path("page").asText(); // 페이지 번호 문자열로 파싱

        Optional<File> file = fileRepository.findByFileName(fileName);
        if (file.isEmpty()) {
            log.debug("❌ fileRepository에서 파일 없음");
            return null;
        }

        String url = file.map(File::getFileUrl).orElse(null);

        // filePath 추가
        String filePath = file.map(File::getFullPath).orElse(null);

        log.debug("🌐 url: {}", url);
        log.debug("📂 filePath: {}", filePath);


        return new FileInfoDto(fileName, page, url, filePath);
    }
}
