package tricode.eduve.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AllCharacter {

    @Id
    @Column(name = "all_character_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allCharacterId;

    @Column(nullable = false)
    private String characterName;

    @Column(nullable = true)
    private String characterImgUrl;
}
