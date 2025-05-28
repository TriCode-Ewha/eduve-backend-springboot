package tricode.eduve.global;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import tricode.eduve.domain.MessageLikePreference;
import tricode.eduve.domain.Preference;
import tricode.eduve.dto.common.FileInfoDto;

import java.util.*;


@RequiredArgsConstructor
@Configuration
public class ChatGptClient {

    @Value("${chatgpt.api-key}")
    private String API_KEY;


    RestTemplate restTemplate = new RestTemplate();


    public ResponseEntity<String> chat(String question, String similarDocuments, Preference preference, String messageLikeAnalysisResult, FileInfoDto fileInfo) {

        HttpHeaders headers = new HttpHeaders(); // HTTP 헤더 생성
        headers.setContentType(MediaType.APPLICATION_JSON); // 요청 본문 타입 설정
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // 수신할 응답 타입 설정
        headers.set("Authorization", "Bearer " + API_KEY); // 인증 헤더에 API 키 추가

        JSONObject messageSystem = new JSONObject(); // 시스템 메시지 JSON 객체 생성
        messageSystem.put("role", "system");  // 역할 설정

        String prompt = buildPrompt(similarDocuments, preference, messageLikeAnalysisResult, fileInfo);
        messageSystem.put("content", prompt); // 시스템 메시지 추가

        JSONObject messageUser = new JSONObject(); // 사용자 메시지 JSON 객체 생성
        messageUser.put("role", "user"); // 역할 설정
        messageUser.put("content", question); // 사용자 메시지 추가

        JSONObject requestBody = new JSONObject(); // 요청 본문을 위한 JSON 객체 생성
        requestBody.put("model", "gpt-4o-2024-05-13"); // 사용할 모델 설정
        requestBody.put("messages", new JSONArray(Arrays.asList(messageSystem, messageUser))); // 메시지 배열 추가

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers); // HTTP 요청 엔티티 생성

        String apiEndpoint = "https://api.openai.com/v1/chat/completions"; // API 엔드포인트 설정
        try {
            // REST API 호출을 통해 응답 받기
            ResponseEntity<String> response = restTemplate.postForEntity(apiEndpoint, request, String.class);

            // 응답 상태 코드 확인
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response;  // 성공적인 응답 반환
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("api 호출 중 오류 발생!"); // 오류 메시지 반환
            }
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("api 호출 중 예외 발생: " + e.getMessage()); // 예외 메시지 반환
        }
    }

    private String buildPrompt(String similarDocuments, Preference preference, String messageLikeAnalysisResult, FileInfoDto fileInfo) {

        // 톤과 설명 수준에 맞는 프롬프트 설정
        String toneInstruction = preference.getTone().getPromptInstruction();
        String levelInstruction = preference.getDescriptionLevel().getPromptInstruction();

        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("너는 학생들에게 모르는 부분을 친절하게 설명해주는 학습 보조 챗봇이야.\n")
                .append(toneInstruction).append("\n")
                .append(levelInstruction).append("\n\n")
                .append("질문에 답할 때는 반드시 제공된 '관련 문서'를 근거로 설명해야 해.\n")
                .append("관련 문서에 없는 내용은 절대 추측하거나 지어내지 마.\n\n");

        // 사용자의 좋아요 기반 분석 결과가 있으면 참고용으로 추가
        if (messageLikeAnalysisResult != null && !messageLikeAnalysisResult.isBlank()) {
            promptBuilder.append("또한 사용자가 선호하는 답변 스타일은 다음과 같아. 이를 참고해서 답변을 구성해줘:\n")
                    .append(messageLikeAnalysisResult).append("\n\n");
        }

        if (fileInfo != null) {
            String filePath = fileInfo.getFilePath();
            String page = fileInfo.getPage();

            promptBuilder.append("이 질문과 관련된 문서는 다음 경로에 있어: '")
                    .append(filePath).append("', 관련 페이지는 ").append(page).append("이야.\n");
            promptBuilder.append("만약 사용자가 문서의 위치나 접근 방법을 묻는다면 이 정보를 활용해서 답변해줘.\n\n");
        }

        promptBuilder.append("다음은 관련 문서야:\n")
                .append(similarDocuments).append("\n");

        return promptBuilder.toString();
    }


    public String analyzeLikedMessages(List<String> likedMessages) {
        // 사용자 선호 스타일 분석을 위한 단일 프롬프트 생성
        String prompt = "다음 메시지는 사용자가 챗봇의 답변에 좋아요를 누른 것들입니다. 이것에 기반하여 사용자의 선호 답변 스타일을 분석해 주세요 (정보는 이것이 전부이므로 추가적인 질문은 절대 하지 마세요. 바로 분석하세요): \n"
                + String.join("\n", likedMessages);

        // HTTP 요청을 위한 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + API_KEY);

        // 사용자 메시지 설정
        JSONObject messageUser = new JSONObject();
        messageUser.put("role", "user");
        messageUser.put("content", prompt);  // 단순히 사용자 메시지로 프롬프트 사용

        // 요청 본문 생성
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o-2024-05-13");
        requestBody.put("messages", new JSONArray(Arrays.asList(messageUser)));

        // HTTP 요청 엔티티 생성
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        String apiEndpoint = "https://api.openai.com/v1/chat/completions"; // API 엔드포인트

        try {
            // API 호출
            ResponseEntity<String> response = restTemplate.postForEntity(apiEndpoint, request, String.class);

            // 응답 확인
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractContentOnly(response.getBody());
            } else {
                throw new RuntimeException("API 호출 중 오류 발생!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("API 호출 중 예외 발생: " + e.getMessage());
        }
    }


    private String extractContentOnly(String responseJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseJson);
            return rootNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("분석 결과 파싱 중 오류 발생", e);
        }
    }


}
