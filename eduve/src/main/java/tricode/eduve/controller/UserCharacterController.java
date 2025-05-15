package tricode.eduve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tricode.eduve.dto.response.character.UserCharacterPreferenceDto;
import tricode.eduve.dto.request.UserCharacterPreferenceRequestDto;
import tricode.eduve.service.UserCharacterService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/userCharacter")
public class UserCharacterController {

    private final UserCharacterService userCharacterService;

    //사용자 캐릭터 정보 설정
    @PatchMapping("/{userId}")
    public ResponseEntity<UserCharacterPreferenceDto> updateUserCharacter(@PathVariable Long userId, @RequestBody UserCharacterPreferenceRequestDto requestDto) {
        return ResponseEntity.ok(userCharacterService.updateUserCharacter(userId, requestDto));
    }

    //사용자 캐릭터 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserCharacterPreferenceDto> getUserCharacterPreference(@PathVariable Long userId) {
        return ResponseEntity.ok(userCharacterService.getUserCharacterPreference(userId));
    }
}
