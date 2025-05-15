package tricode.eduve.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tricode.eduve.repository.PreferenceRepository;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Preference {

    @Id
    @Column(name = "preference_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long preferenceId;

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    @Column(nullable = false)
    private Tone tone;

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    @Column(nullable = false)
    private DescriptionLevel descriptionLevel;
}
