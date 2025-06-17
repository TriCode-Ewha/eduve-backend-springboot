package tricode.eduve.global;


import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tricode.eduve.domain.User;

import java.io.IOException;
import java.util.Map;


@Component
public class FlaskComponent {

    private final RestTemplate restTemplate;

    public FlaskComponent() {
        this.restTemplate = new RestTemplate();
    }


    // 유사도 검색 flask API 호출
    public String findSimilarDocuments(String question, Long userId, User teacher) {
        String flaskApiUrl = "http://172.31.45.158:5000/search";  // Flask API URL (로컬에서 Flask 실행 중이라면 localhost 사용)
        //String flaskApiUrl = "http://localhost:5000/search";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody;
        if(teacher != null){
            // 요청 바디 설정 (JSON 포맷)
            requestBody = "{ \"query\": \"" + question + "\", \"userId\": \"" + userId + "\", \"teacherId\": \"" + teacher.getUserId() + "\" }";
        }else{
            // 요청 바디 설정 (JSON 포맷)
            requestBody = "{ \"query\": \"" + question + "\", \"userId\": \"" + userId + "\" }";
        }


        // HTTP 요청 엔티티 생성
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Flask API 호출 (POST 요청)
        ResponseEntity<String> response = restTemplate.exchange(flaskApiUrl, HttpMethod.POST, entity, String.class);

        // 응답 데이터 추출
        return response.getBody();
    }

    public String extractTopic(String userMessage) {
        String flaskApiUrl = "http://172.31.45.158:5000/extractTopic";  // Flask API URL (로컬에서 Flask 실행 중이라면 localhost 사용)
        //String flaskApiUrl = "http://localhost:5000/extractTopic";

        ResponseEntity<Map> response = restTemplate.postForEntity(flaskApiUrl, Map.of("message", userMessage), Map.class);
        return (String) response.getBody().get("topic");
    }

    public double calculateSimilarity(String lastTopic, String newTopic) {
        String flaskApiUrl = "http://172.31.45.158:5000/calculateSimilarity";  // Flask API URL (로컬에서 Flask 실행 중이라면 localhost 사용)
        //String flaskApiUrl = "http://localhost:5000/calculateSimilarity";

        ResponseEntity<Map> response = restTemplate.postForEntity(flaskApiUrl, Map.of("topic1", lastTopic, "topic2", newTopic), Map.class);
        return (double) response.getBody().get("similarity");
    }

    // 임베딩 API 호출
    public String embedDocument(MultipartFile file, String filename, Long userId) throws IOException {
        String url = "http://172.31.45.158:5000/embedding";
        //String url = "http://localhost:5000/embedding";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        String S3filename = userId + "/" + filename;

        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), S3filename));
        body.add("userId", userId.toString());  // user_id를 form-data에 추가
        body.add("title", S3filename); // filename을 from-data에 추가

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            // Flask에서 에러 메시지를 JSON 형태로 반환하는 경우 처리 가능
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return "Flask server error: " + response.getBody();
            }
        } catch (Exception e) {
            // RestTemplate에서 발생하는 예외 (ex. 서버 다운, 500 오류 등)
            return "Failed to connect to Flask server: " + e.getMessage();
        }
    }
}
