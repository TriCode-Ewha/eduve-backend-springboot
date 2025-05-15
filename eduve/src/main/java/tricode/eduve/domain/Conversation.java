package tricode.eduve.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tricode.eduve.global.CreatedTimeEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Conversation extends CreatedTimeEntity {

    // 대화 세션
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "conversation_name",  nullable = false)
    private String conversationName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @Column(name = "last_topic", nullable = false)
    private String lastTopic;

    @Column(nullable = false)
    private LocalDateTime updatedTime; 


    public Conversation(String conversationName, User user) {
        this.conversationName = conversationName;
        this.user = user;
        this.lastTopic = conversationName;
        updatedTime = LocalDateTime.now();
    }

    // 메시지 추가 메서드
    public void addMessage(Message message) {
        message.setConversation(this);  // 양방향 관계 설정
        this.messages.add(message);
    }

    public void updateLastTopic(String newTopic) {
        this.lastTopic = newTopic;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
