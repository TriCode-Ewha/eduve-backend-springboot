package tricode.eduve.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tricode.eduve.dto.request.MessageRequestDto;
import tricode.eduve.dto.response.message.MessageUnitDto;
import tricode.eduve.dto.response.message.UserMessage;
import tricode.eduve.dto.response.message.BotMessage;
import tricode.eduve.service.ChatService;
import tricode.eduve.domain.Message;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void startConversation_shouldReturnBotAndUserMessage() throws Exception {
        // Given
        Message userMsg = createMessage(1L, "안녕하세요", LocalDateTime.now());
        Message botMsg = createMessage(2L, "반갑습니다. 무엇을 도와드릴까요?", LocalDateTime.now());

        UserMessage userMessage = new UserMessage(userMsg);
        BotMessage botMessage = new BotMessage(botMsg, userMsg.getMessageId());

        MessageUnitDto responseDto = new MessageUnitDto(userMessage, botMessage, null);

        given(chatService.startConversation(any(), eq(1L), eq(0L), eq(0L)))
                .willReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/chat/start/1")
                        .param("graph", "0")
                        .param("url", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MessageRequestDto("안녕하세요"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userMessage.question").value("안녕하세요"))
                .andExpect(jsonPath("$.botMessage.answer").value("반갑습니다. 무엇을 도와드릴까요?"));
    }

    // 유틸: 테스트용 Message 객체 생성
    private Message createMessage(Long id, String content, LocalDateTime createdTime) {
        Message message = new Message();
        message.setMessageId(id);
        message.setContent(content);
        message.setCreatedTime(createdTime);
        return message;
    }
}

