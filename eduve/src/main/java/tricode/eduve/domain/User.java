package tricode.eduve.domain;

import jakarta.persistence.*;
import lombok.*;
import tricode.eduve.global.CreatedTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends CreatedTimeEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // 유저 식별 아이디

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String username; // 로그인용 아이디

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role; // 학생 선생님 구분

    @Column(nullable = true, unique = true)
    private String teacherUsername; // 학생이라면 선생님 로그인용 아이디 저장, 선생님은 null

    @ManyToMany
    @JoinTable(
            name = "user_lecture", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "user_id"), // 현재 엔티티의 외래키
            inverseJoinColumns = @JoinColumn(name = "lecture_id") // 반대편 엔티티의 외래키
    )
    private List<Lecture> lectures = new ArrayList<>();

}
