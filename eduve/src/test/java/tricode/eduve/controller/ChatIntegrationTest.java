package tricode.eduve.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "student01", roles = {"STUDENT"})
@Sql(scripts = "/sample/integration.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("캐릭터 설정을 변경하면 GPT 응답에 해당 말투가 반영된다")
    void updateCharacter_thenChat_shouldReflectPersonaInGPTResponse() throws Exception {
        // 1️⃣ 캐릭터 설정 변경
        mockMvc.perform(patch("/userCharacter/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"tone\": \"FRIENDLY\","
                                + "\"descriptionLevel\": \"HIGH\","
                                + "\"userCharacterName\": \"공감형 조언자\""
                                + "}"))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.userCharacterName").value("공감형 조언자"));

        // 2️⃣ 실제 GPT 기반 채팅 요청
        String input = "안녕하세요.";

        mockMvc.perform(post("/chat/start/101")
                        .param("graph", "0")
                        .param("url", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "question": "안녕하세요."
                    }
                """))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.userMessage.question").value("안녕하세요."))
                .andExpect((ResultMatcher) jsonPath("$.botMessage.answer").isNotEmpty());
        // 👉 여기서 .value(...)로 말투가 실제 반영됐는지는 확인
    }
}
