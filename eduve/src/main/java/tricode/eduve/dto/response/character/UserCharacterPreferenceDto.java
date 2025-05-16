package tricode.eduve.dto.response.character;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tricode.eduve.domain.UserCharacter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCharacterPreferenceDto {
    private Long characterId;
    private String userCharacterName;
    private Long userCharacterId;
    private String userCharacterImgUrl;
    private String tone;
    private String descriptionLevel;

    public static UserCharacterPreferenceDto from(UserCharacter userCharacter) {
        return new UserCharacterPreferenceDto(
                userCharacter.getCharacter().getAllCharacterId(),
                userCharacter.getUserCharacterName(),
                userCharacter.getUserCharacterId(),
                userCharacter.getCharacter().getCharacterImgUrl(),
                userCharacter.getPreference().getTone().name(),  // Enum을 String으로 변환
                userCharacter.getPreference().getDescriptionLevel().name()  // Enum을 String으로 변환
        );
    }
}
