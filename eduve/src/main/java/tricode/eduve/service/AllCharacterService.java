package tricode.eduve.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.AllCharacter;
import tricode.eduve.dto.response.character.AllCharacterResponseDto;
import tricode.eduve.dto.response.character.CharacterUnitDto;
import tricode.eduve.repository.AllCharacterRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AllCharacterService {
    private final AllCharacterRepository allCharacterRepository;

    public AllCharacterResponseDto getAllCharacters() {
        List<AllCharacter> allCharacters = allCharacterRepository.findAll();
        List<CharacterUnitDto> characterDtos = allCharacters.stream()
                .map(character -> new CharacterUnitDto(character.getAllCharacterId(), character.getCharacterName(), character.getCharacterImgUrl()))
                .collect(Collectors.toList());
        return new AllCharacterResponseDto(characterDtos);
    }

    public CharacterUnitDto getOneCharacters(Long allCharacterId) {
        return allCharacterRepository.findById(allCharacterId)
                .map(character -> new CharacterUnitDto(character.getAllCharacterId(), character.getCharacterName(), character.getCharacterImgUrl()))
                .orElseThrow(() -> new RuntimeException("Character not found with id " + allCharacterId));
    }
}
