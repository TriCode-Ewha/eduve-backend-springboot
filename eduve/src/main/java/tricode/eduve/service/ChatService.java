package tricode.eduve.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import tricode.eduve.repository.MessageRepository;
import tricode.eduve.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
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


    public MessageUnitDto startConversation(MessageRequestDto requestDto, Long userId, Long graph) throws Exception {

        String userMessage = requestDto.getQuestion();

        // 1. Conversation 처리 (주제 유사도검색 + 1시간 기준)
        Message message = conversationService.processUserMessage(userId, userMessage);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        String similarDocuments = null;
        if (user.getRole().equals("ROLE_Student")) {
            System.out.println("진입 성공");
            Optional<User> teacher = userRepository.findByUsernameAndRole(user.getTeacherUsername(), "ROLE_Teacher");
            if (teacher.isPresent()) {
                // 질문 유사도 검색
                similarDocuments = flaskComponent.findSimilarDocuments(userMessage, userId, teacher.get());
                // 선생님 잘 찾았는지 로그 찍기
                User teacherUser = teacher.get();
                System.out.println("teacher username: " + teacherUser.getUsername());
            }else{
                similarDocuments = flaskComponent.findSimilarDocuments(userMessage, userId, null);
            }
        }else{
            // 질문 유사도 검색
            similarDocuments = flaskComponent.findSimilarDocuments(userMessage, userId, null);
        }

        // 유사도검색 결과에서 파일명 추출해서 파일 url 반환
        FileInfoDto fileInfo = extractFirstFileInfo(similarDocuments);

        // 사용자가 설정한 TONE/DISCRIPTIONLEVEL 조회
        Preference userPreference = userCharacterService.getPrefernceByUserId(userId); // tone, explanationLevel 포함

        // 사용자 좋아요 분석 조회
        String analysisResult = messageLikePreferenceRepository.findByUser(user)
                .map(MessageLikePreference::getAnalysisResult)
                .orElse(null);

        // 좋아요 분석이 있으면 chatGPT로 같이 보내기
        ResponseEntity<String> response;
        if (analysisResult != null && !analysisResult.isBlank()) {
            response = chatGptClient.chat(userMessage, similarDocuments, userPreference, analysisResult, fileInfo, graph);
        } else {
            response = chatGptClient.chat(userMessage, similarDocuments, userPreference, null, fileInfo, graph);
        }
        String parsedResponse = parseResponse(response);

        // 3. 봇 응답 저장
        Message botMessage = Message.createBotResponse(message.getConversation(), parsedResponse, message);
        conversationService.saveBotMessage(botMessage);

        return MessageUnitDto.from(message,botMessage, fileInfo);
    }

    // 유사도 검색 결과에서 파일 제목과 url 추출
    public FileInfoDto extractFirstFileInfo(String similarDocuments) throws Exception {

        final double SCORE_THRESHOLD = 0.3900;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(similarDocuments);
        JsonNode results = root.path("results");

        // results가 배열이 아니거나 비어 있으면 null 반환
        if (!results.isArray() || results.isEmpty()) {
            return null;
        }

        JsonNode firstResult = results.get(0);
        if (firstResult == null || firstResult.isEmpty()) {
            return null;
        }

        String fileName = firstResult.path("file_name").asText();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        // score 확인
        double score = firstResult.path("score").asDouble();
        if (score >= SCORE_THRESHOLD) {
            return null;
        }

        String page = firstResult.path("page").asText(); // 페이지 번호 문자열로 파싱

        Optional<File> file = fileRepository.findByFileName(fileName);
        String url = file.map(File::getFileUrl).orElse(null);

        // filePath 추가
        String filePath = file.map(File::getFullPath).orElse(null);

        return new FileInfoDto(fileName, page, url, filePath);
    }
}
