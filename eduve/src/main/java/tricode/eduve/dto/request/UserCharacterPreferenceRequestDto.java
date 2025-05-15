package tricode.eduve.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCharacterPreferenceRequestDto {
    private Long characterId;
    private String userCharacterName; // 사용자 캐릭터 이름
    private String tone; // 말투
    private String descriptionLevel; // 설명 난이도
}
