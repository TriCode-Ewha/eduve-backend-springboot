package tricode.eduve.dto.response.character;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AllCharacterResponseDto {
    List<CharacterUnitDto> AllCharacterList;
}
