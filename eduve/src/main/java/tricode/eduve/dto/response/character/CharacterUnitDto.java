package tricode.eduve.dto.response.character;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tricode.eduve.domain.AllCharacter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CharacterUnitDto {
    private Long characterId;
    private String characterName;
    private String characterImgUrl;

    public static CharacterUnitDto from(AllCharacter character) {
        return new CharacterUnitDto(
                character.getAllCharacterId(),
                character.getCharacterName(),
                character.getCharacterImgUrl()
        );
    }
}
