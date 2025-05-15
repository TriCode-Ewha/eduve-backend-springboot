package tricode.eduve.domain;

import jakarta.persistence.*;
import lombok.*;
import tricode.eduve.global.CreatedTimeEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends CreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false)
    private Long MessageId;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false)
    private boolean isUserMessage; // true: 사용자 질문, false: 챗봇 응답

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 메시지 내용

    // 어떤 질문에 대한 응답인지 저장 (챗봇 응답일 경우만 해당)
    @ManyToOne
    @JoinColumn(name = "question_message_id", nullable = true)
    private Message questionMessage;


    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private MessageLike like;


    // 사용자 메시지 생성자
    public static Message createUserMessage(Conversation conversation, String content) {
        return Message.builder()
                .conversation(conversation)
                .isUserMessage(true)
                .content(content)
                .build();
    }

    // 챗봇 응답 메시지 생성자 (질문 메시지와 연결)
    public static Message createBotResponse(Conversation conversation, String content, Message questionMessage) {
        return Message.builder()
                .conversation(conversation)
                .isUserMessage(false)
                .content(content)
                .questionMessage(questionMessage) // 어떤 질문에 대한 응답인지 설정
                .build();
    }
}
