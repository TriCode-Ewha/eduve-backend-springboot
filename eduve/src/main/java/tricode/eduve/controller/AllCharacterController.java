package tricode.eduve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tricode.eduve.dto.response.character.AllCharacterResponseDto;
import tricode.eduve.dto.response.character.CharacterUnitDto;
import tricode.eduve.service.AllCharacterService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/characters")
public class AllCharacterController {
    private final AllCharacterService allCharacterService;

    // 모든 기본 캐릭터 정보 조회
    @GetMapping
    public ResponseEntity<AllCharacterResponseDto> getAllCharacters() {
        return ResponseEntity.ok(allCharacterService.getAllCharacters());
    }

    // 캐릭터 하나 조회
    @GetMapping("/{allCharacterId}")
    public ResponseEntity<CharacterUnitDto> getOneCharacters(@PathVariable Long allCharacterId) {
        return ResponseEntity.ok(allCharacterService.getOneCharacters(allCharacterId));
    }
}
