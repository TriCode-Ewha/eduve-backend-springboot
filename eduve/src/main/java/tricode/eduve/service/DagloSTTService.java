package tricode.eduve.service;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DagloSTTService {

    @Value("${daglo.stt.api-key}")
    private String apiKey;

    @Value("${daglo.stt.api-url}")
    private String apiUrl;

    public Optional<String> requestTranscription(String fileUrl) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(apiUrl); // ex: https://apis.daglo.ai/stt/v1/async/transcripts
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            JSONObject audio = new JSONObject();
            JSONObject source = new JSONObject();

            source.put("url", fileUrl);
            audio.put("source", source);
            body.put("audio", audio);

            StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(result);

            return Optional.ofNullable(json.optString("rid", null));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<String> getTranscriptionResult(String rid) {
        String statusUrl = apiUrl + "/" + rid;  // ex: https://apis.daglo.ai/stt/v1/async/transcripts/{rid}

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(statusUrl);
            get.setHeader("Authorization", "Bearer " + apiKey);

            HttpResponse response = client.execute(get);
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(result);

            String status = json.optString("status", "");

            if (!"transcribed".equalsIgnoreCase(status)) {
                return Optional.empty(); // 아직 처리되지 않았거나 실패한 경우
            }

            if (!json.has("sttResults")) return Optional.empty();

            JSONArray sttResults = json.getJSONArray("sttResults");
            if (sttResults.length() == 0) return Optional.empty();

            JSONObject firstResult = sttResults.getJSONObject(0);
            return Optional.ofNullable(firstResult.optString("transcript", ""));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
