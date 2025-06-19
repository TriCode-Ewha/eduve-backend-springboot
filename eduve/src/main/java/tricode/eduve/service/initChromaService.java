package tricode.eduve.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class initChromaService {
    private final RestTemplate restTemplate = new RestTemplate();

    public void callDeleteApi(Long userId) {
        String url = "http://localhost:5000/delete_all?user_id=" + userId;

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("외부 API 삭제 실패: " + response.getStatusCode());
        }
    }
}
