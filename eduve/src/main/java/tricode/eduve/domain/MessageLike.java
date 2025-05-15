package tricode.eduve.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Setter
public class MessageLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long MessageLikeId;


    @OneToOne
    @JoinColumn(name = "message_id")
    private Message message;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // User 연결
    private User user;

    public MessageLike(Message message) {
        this.message = message;
        this.user = message.getConversation().getUser();
    }
}
