package tricode.eduve.global;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import tricode.eduve.domain.Preference;
import tricode.eduve.dto.common.FileInfoDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;



@RequiredArgsConstructor
@Configuration
public class ChatGptClient {

    @Value("${chatgpt.api-key}")
    private String API_KEY;


    RestTemplate restTemplate = new RestTemplate();


    public ResponseEntity<String> chat(String question, String similarDocuments, Preference preference, String messageLikeAnalysisResult, FileInfoDto fileInfo, Long graph) {

        if(graph == 0){
            HttpHeaders headers = new HttpHeaders(); // HTTP 헤더 생성
            headers.setContentType(MediaType.APPLICATION_JSON); // 요청 본문 타입 설정
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // 수신할 응답 타입 설정
            headers.set("Authorization", "Bearer " + API_KEY); // 인증 헤더에 API 키 추가


            String prompt = buildPrompt(question,
                    similarDocuments,
                    preference,
                    messageLikeAnalysisResult,
                    fileInfo);


            JSONObject messageSystem = new JSONObject(); // 시스템 메시지 JSON 객체 생성
            messageSystem.put("role", "system");  // 역할 설정
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
        else{
            HttpHeaders headers = new HttpHeaders(); // HTTP 헤더 생성
            headers.setContentType(MediaType.APPLICATION_JSON); // 요청 본문 타입 설정
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // 수신할 응답 타입 설정
            headers.set("Authorization", "Bearer " + API_KEY); // 인증 헤더에 API 키 추가
            headers.set("OpenAI-Beta", "assistants=v2");

            // Assistant API 방식 전환 (그래프 분석용)
            //String prompt = buildPromptWithGraph(preference, messageLikeAnalysisResult, fileInfo);
            String prompt = buildPromptWithGraph(question,
                    preference,
                    messageLikeAnalysisResult,
                    fileInfo);
            String fileUrl = fileInfo.getFileUrl();
            String fileId;

            try {
                fileId = uploadFileToOpenAI(fileUrl);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패: " + e.getMessage());
            }

            HttpEntity<String> threadEntity = new HttpEntity<>("{}", headers);


            try {
                ResponseEntity<String> threadResponse = restTemplate.postForEntity("https://api.openai.com/v1/threads", threadEntity, String.class);
                String threadId = new JSONObject(threadResponse.getBody()).getString("id");

                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt + "\n\n질문: " + question);

                // ─── 첨부 파일을 보낼 때는 tools에 "file_search"를 지정해야 합니다 ───
                JSONArray attachments = new JSONArray();
                JSONObject attachment = new JSONObject();
                attachment.put("file_id", fileId);
                // File Search 도구로 분석하도록 tools에 type="file_search" 추가
                JSONArray toolsArray = new JSONArray();
                JSONObject tool = new JSONObject();
                tool.put("type", "file_search");
                toolsArray.put(tool);
                attachment.put("tools", toolsArray);
                attachments.put(attachment);
                message.put("attachments", attachments);
                // ─────────────────────────────────────────────────────────────────



                HttpEntity<String> messageEntity = new HttpEntity<>(message.toString(), headers);
                restTemplate.postForEntity(
                        "https://api.openai.com/v1/threads/" + threadId + "/messages",
                        messageEntity,
                        String.class
                );

                // 3. Assistant Run 요청
                JSONObject runRequest = new JSONObject();
                runRequest.put("assistant_id", "asst_wzOgAJG8KKcan93VGCR6ENfz");
                HttpEntity<String> runEntity = new HttpEntity<>(runRequest.toString(), headers);

                ResponseEntity<String> runResponse = restTemplate.postForEntity(
                        "https://api.openai.com/v1/threads/" + threadId + "/runs",
                        runEntity,
                        String.class);
                String runId = new JSONObject(runResponse.getBody()).getString("id");

                // 3. Run 완료 대기 (단순 폴링)
                while (true) {
                    Thread.sleep(1000);
                    // GET 요청에도 인증 헤더를 포함하려면 exchange() 사용
                    HttpEntity<Void> statusEntity = new HttpEntity<>(headers);
                    ResponseEntity<String> runStatusResponse = restTemplate.exchange(
                            "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId,
                            HttpMethod.GET,
                            statusEntity,
                            String.class
                    );
                    String status = new JSONObject(runStatusResponse.getBody()).getString("status");
                    if ("completed".equals(status)) break;
                }

                // 이후 메시지 조회 부분도 동일하게 수정
                HttpEntity<Void> messageStatusEntity = new HttpEntity<>(headers);
                ResponseEntity<String> messageResponse = restTemplate.exchange(
                        "https://api.openai.com/v1/threads/" + threadId + "/messages",
                        HttpMethod.GET,
                        messageStatusEntity,
                        String.class
                );
                JSONArray messages = new JSONObject(messageResponse.getBody()).getJSONArray("data");
                // ──────────────────────────────────────────────────────────────────────

                System.out.println(messages);

                for (int i = 0; i < messages.length(); i++) {
                    JSONObject msg = messages.getJSONObject(i);
                    if ("assistant".equals(msg.getString("role"))) {
                        // content 배열의 첫 번째 객체 내부에 있는 text.value 값을 꺼냅니다.
                        JSONObject firstContent = msg.getJSONArray("content").getJSONObject(0);
                        JSONObject textObj = firstContent.getJSONObject("text");
                        String reply = textObj.getString("value");
                        return ResponseEntity.ok(reply);
                    }
                }

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Assistant 응답 없음");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assistant API 호출 중 예외 발생: " + e.getMessage());
            }
        }

//        JSONObject messageSystem = new JSONObject(); // 시스템 메시지 JSON 객체 생성
//        messageSystem.put("role", "system");  // 역할 설정
//
//        String prompt = null;
//        if(graph == 0){
//            // 그래프 분석 아닐 경우
//            prompt = buildPrompt(similarDocuments, preference, messageLikeAnalysisResult, fileInfo);
//        }else{
//            // 그래프 분석일 경우
//            prompt = buildPromptWithGraph(preference, messageLikeAnalysisResult, fileInfo);
//        }
//        messageSystem.put("content", prompt); // 시스템 메시지 추가
//
//        JSONObject messageUser = new JSONObject(); // 사용자 메시지 JSON 객체 생성
//        messageUser.put("role", "user"); // 역할 설정
//        messageUser.put("content", question); // 사용자 메시지 추가
//
//        JSONObject requestBody = new JSONObject(); // 요청 본문을 위한 JSON 객체 생성
//        requestBody.put("model", "gpt-4o-2024-05-13"); // 사용할 모델 설정
//        requestBody.put("messages", new JSONArray(Arrays.asList(messageSystem, messageUser))); // 메시지 배열 추가
//
//        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers); // HTTP 요청 엔티티 생성
//
//        String apiEndpoint = "https://api.openai.com/v1/chat/completions"; // API 엔드포인트 설정
//        try {
//            // REST API 호출을 통해 응답 받기
//            ResponseEntity<String> response = restTemplate.postForEntity(apiEndpoint, request, String.class);
//
//            // 응답 상태 코드 확인
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return response;  // 성공적인 응답 반환
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("api 호출 중 오류 발생!"); // 오류 메시지 반환
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("api 호출 중 예외 발생: " + e.getMessage()); // 예외 메시지 반환
//        }
    }

    private String uploadFileToOpenAI(String fileUrl) throws Exception {
        //주어진 fileUrl(예: presigned S3 URL)로 HTTP GET 요청을 설정합니다.
        URI uri = new URI(fileUrl); // 인코딩 손상 없이 보존
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);

        File tempFile = File.createTempFile("upload", ".pdf");
        try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        fileHeaders.set("Authorization", "Bearer " + API_KEY);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("purpose", "assistants");
        body.add("file", new FileSystemResource(tempFile));

        HttpEntity<MultiValueMap<String, Object>> fileRequest = new HttpEntity<>(body, fileHeaders);
        ResponseEntity<String> uploadResponse = restTemplate.postForEntity("https://api.openai.com/v1/files", fileRequest, String.class);
        tempFile.delete();

        if (uploadResponse.getStatusCode() == HttpStatus.OK) {
            return new JSONObject(uploadResponse.getBody()).getString("id");
        } else {
            throw new IOException("파일 업로드 실패: " + uploadResponse.getBody());
        }
    }

    private String buildPrompt(String question, String similarDocuments, Preference preference, String messageLikeAnalysisResult, FileInfoDto fileInfo) {

        // 톤과 설명 수준에 맞는 프롬프트 설정
        String toneInstruction = preference.getTone().getPromptInstruction();
        String levelInstruction = preference.getDescriptionLevel().getPromptInstruction();

        StringBuilder sb = new StringBuilder();

        /* 1. 시스템 역할 */
        sb.append("[시스템 역할]\n")
                .append("너는 학생들에게 모르는 부분을 친절하고 명확하게 설명해주는 학습 보조 챗봇이야.\n\n");

        /* 2. 톤 & 설명 수준 */
        sb.append("[톤(Tone) 및 설명 수준(Level) 지시문]\n")
                .append(" - 톤 지시문: ").append(toneInstruction).append("\n")
                .append(" - 설명 수준 지시문: ").append(levelInstruction).append("\n\n");

        /* 3. 중요 규칙 */
        sb.append("[중요 규칙]\n")
                .append(" 1) 질문에 답할 때는 반드시 제공된 '관련 문서'를 근거로 설명해야 한다.\n")
                .append(" 2) 관련 문서에 없는 내용은 절대 추측하거나 지어내지 않아야 한다.\n\n");

        /* 4. 문서 정보 (옵션) */
        if (fileInfo != null && fileInfo.getFilePath() != null && fileInfo.getPage() != null) {
            sb.append("[문서 정보]\n")
                    .append(" - 문서 경로: '").append(fileInfo.getFilePath()).append("'\n")
                    .append(" - 관련 페이지: ").append(fileInfo.getPage()).append("\n")
                    .append(" (※ 사용자가 문서 위치나 접근 방법을 묻는다면 이 정보를 활용하여 안내하세요)\n\n");
        }

        /* 5. 사용자 선호 정보 */
        if (messageLikeAnalysisResult != null && !messageLikeAnalysisResult.isBlank()) {
            sb.append("[추가 사용자 선호 정보]\n")
                    .append(messageLikeAnalysisResult).append("\n\n");
        }

        /* 6. 관련 문서 본문 */
        sb.append("[관련 문서 본문]\n")
                .append(Optional.ofNullable(similarDocuments).orElse("(관련 문서 미제공)"))
                .append("\n\n");

        /* 7. 질문 유형 분류 고정 안내 */
        sb.append("[질문 유형 분류]\n")
                .append(" • 질문이 “내용(Meaning) 기반 설명”인지, “위치(Location) 기반 정보” 요청인지 파악한다.  \n")
                .append("   – **내용 기반 설명 질문**:  \n")
                .append("     예시) “ΔU = Q – W 유도 과정을 단계별로 설명해 주세요.”  \n")
                .append("     → 이 경우 문서 본문을 근거로 **구조화된 설명**(예: 개념 정의, 유도 과정, 예시 적용)을 제공해야 한다.  \n\n")
                .append("   – **위치 기반 질문**:  \n")
                .append("     예시) “‘열역학 제1법칙’ 정의 부분이 문서 어디에 있나요?”  \n")
                .append("     → 이 경우 문서에서 해당 키워드(‘열역학 제1법칙’)가 포함된 **페이지 번호 또는 섹션**을 정확히 찾아 안내해야 한다.  \n\n")
                .append("   – **혼합형 질문**:  \n")
                .append("     예시) “‘열역학 제1법칙’이 정의된 부분을 23~25쪽에서 찾아 설명해 주세요.”  \n")
                .append("     → 먼저 위치(23~25쪽)를 안내한 뒤, 해당 위치를 근거로 **내용 기반 설명**을 함께 제공한다.\n\n");

        /* 8. 질문 원문 */
        sb.append("[질문]\n")
                .append(question)
                .append("\n\n");

        /* 9. 예상 답변 형식 */
        sb.append("[예상 답변 형식]\n")
                .append("   1) **내용 기반 설명 질문일 때**  \n")
                .append("      • 도입부: 간단한 인사 및 답변 의도 언급  \n")
                .append("      • 본문:  \n")
                .append("        ① 개념 정의  \n")
                .append("        ② 공식/이론 유도 과정  \n")
                .append("        ③ 구체적인 예시 적용  \n")
                .append("      • 결론부: 핵심 요약 및 추가 학습 팁 제공  \n\n")
                .append("   2) **위치 기반 질문일 때**  \n")
                .append("      • 도입부: 간단한 인사 및 답변 의도 언급  \n")
                .append("      • 본문:  \n")
                .append("        ① 문서 파일명 및 페이지 번호(또는 섹션) 안내  \n")
                .append("        ② 해당 위치에서 찾을 수 있는 키워드 또는 문장 인용  \n")
                .append("      • 결론부: 추가로 확인할 수 있는 문서 내 인접 섹션 안내  \n\n")
                .append("   3) **혼합형 질문일 때**  \n")
                .append("      • 도입부: 간단한 인사 및 답변 의도 언급  \n")
                .append("      • 본문:  \n")
                .append("        ① 위치 안내  \n")
                .append("        ② 해당 위치를 근거로 내용 설명(①, ②, ③ 단계)  \n")
                .append("      • 결론부: 핵심 요약 및 추가 학습 팁 제공  \n\n")
                .append("   ---------------------------------------------\n")
                .append("   [예시 답변 시작]\n")
                .append("   안녕하세요! 질문하신 내용을 차례대로 설명드리겠습니다.\n")
                .append("   ... (예시 답변 생략) ...\n")
                .append("   [예시 답변 끝]\n")
                .append("   ---------------------------------------------\n");

        return sb.toString();
    }



    private String buildPromptWithGraph(String question, Preference preference, String messageLikeAnalysisResult, FileInfoDto fileInfo) {

        // 톤과 설명 수준에 맞는 프롬프트 설정
        String toneInstruction = preference.getTone().getPromptInstruction();
        String levelInstruction = preference.getDescriptionLevel().getPromptInstruction();

        StringBuilder sb = new StringBuilder();

        /* 1. 시스템 역할 */
        sb.append("[시스템 역할]\n")
                .append("너는 학생들에게 모르는 부분을 친절하고 명확하게 설명해주는 학습 보조 챗봇이야.\n\n");

        /* 2. 톤 & 설명 수준 */
        sb.append("[톤(Tone) 및 설명 수준(Level) 지시문]\n")
                .append(" - 톤 지시문: ").append(toneInstruction).append("\n")
                .append(" - 설명 수준 지시문: ").append(levelInstruction).append("\n\n");

        /* 3. 중요 규칙 */
        sb.append("[중요 규칙]\n")
                .append(" 1) 질문에 답할 때는 반드시 제공된 '관련 문서'를 근거로 설명해야 한다.\n")
                .append(" 2) 관련 문서에 없는 내용은 절대 추측하거나 지어내지 않아야 한다.\n\n")
                .append(" 3) 파일에서 사용과 질문과 관련된 표/그래프를 분석해서 대답해야한다.\n\n");

        /* 4. 문서 정보 (옵션) */
        if (fileInfo != null && fileInfo.getFilePath() != null && fileInfo.getPage() != null) {
            sb.append("[문서 정보]\n")
                    .append(" - 문서 경로: '").append(fileInfo.getFilePath()).append("'\n")
                    .append(" - 관련 페이지: ").append(fileInfo.getPage()).append("\n")
                    .append(" (※ 사용자가 문서 위치나 접근 방법을 묻는다면 이 정보를 활용하여 안내하세요)\n\n");
        }

        /* 5. 사용자 선호 정보 */
        if (messageLikeAnalysisResult != null && !messageLikeAnalysisResult.isBlank()) {
            sb.append("[추가 사용자 선호 정보]\n")
                    .append(messageLikeAnalysisResult).append("\n\n");
        }

        /* 7. 질문 유형 분류 고정 안내 */
        sb.append("[질문 유형 분류]\n")
                .append(" • 질문이 “내용(Meaning) 기반 설명”인지, “위치(Location) 기반 정보” 요청인지 파악한다.  \n")
                .append("   – **내용 기반 설명 질문**:  \n")
                .append("     예시) “ΔU = Q – W 유도 과정을 단계별로 설명해 주세요.”  \n")
                .append("     → 이 경우 문서 본문을 근거로 **구조화된 설명**(예: 개념 정의, 유도 과정, 예시 적용)을 제공해야 한다.  \n\n")
                .append("   – **위치 기반 질문**:  \n")
                .append("     예시) “‘열역학 제1법칙’ 정의 부분이 문서 어디에 있나요?”  \n")
                .append("     → 이 경우 문서에서 해당 키워드(‘열역학 제1법칙’)가 포함된 **페이지 번호 또는 섹션**을 정확히 찾아 안내해야 한다.  \n\n")
                .append("   – **혼합형 질문**:  \n")
                .append("     예시) “‘열역학 제1법칙’이 정의된 부분을 23~25쪽에서 찾아 설명해 주세요.”  \n")
                .append("     → 먼저 위치(23~25쪽)를 안내한 뒤, 해당 위치를 근거로 **내용 기반 설명**을 함께 제공한다.\n\n");

        /* 8. 질문 원문 */
        sb.append("[질문]\n")
                .append(question)
                .append("\n\n");

        /* 9. 예상 답변 형식 */
        sb.append("[예상 답변 형식]\n")
                .append("   1) **내용 기반 설명 질문일 때**  \n")
                .append("      • 도입부: 간단한 인사 및 답변 의도 언급  \n")
                .append("      • 본문:  \n")
                .append("        ① 개념 정의  \n")
                .append("        ② 공식/이론 유도 과정  \n")
                .append("        ③ 구체적인 예시 적용  \n")
                .append("      • 결론부: 핵심 요약 및 추가 학습 팁 제공  \n\n")
                .append("   2) **위치 기반 질문일 때**  \n")
                .append("      • 도입부: 간단한 인사 및 답변 의도 언급  \n")
                .append("      • 본문:  \n")
                .append("        ① 문서 파일명 및 페이지 번호(또는 섹션) 안내  \n")
                .append("        ② 해당 위치에서 찾을 수 있는 키워드 또는 문장 인용  \n")
                .append("      • 결론부: 추가로 확인할 수 있는 문서 내 인접 섹션 안내  \n\n")
                .append("   3) **혼합형 질문일 때**  \n")
                .append("      • 도입부: 간단한 인사 및 답변 의도 언급  \n")
                .append("      • 본문:  \n")
                .append("        ① 위치 안내  \n")
                .append("        ② 해당 위치를 근거로 내용 설명(①, ②, ③ 단계)  \n")
                .append("      • 결론부: 핵심 요약 및 추가 학습 팁 제공  \n\n")
                .append("   ---------------------------------------------\n")
                .append("   [예시 답변 시작]\n")
                .append("   안녕하세요! 질문하신 내용을 차례대로 설명드리겠습니다.\n")
                .append("   ... (예시 답변 생략) ...\n")
                .append("   [예시 답변 끝]\n")
                .append("   ---------------------------------------------\n");

        return sb.toString();
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
