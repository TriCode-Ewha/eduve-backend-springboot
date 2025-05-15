package tricode.eduve.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tricode.eduve.domain.*;
import tricode.eduve.dto.response.character.UserCharacterPreferenceDto;
import tricode.eduve.dto.request.UserCharacterPreferenceRequestDto;
import tricode.eduve.repository.AllCharacterRepository;
import tricode.eduve.repository.PreferenceRepository;
import tricode.eduve.repository.UserCharacterRepository;
import tricode.eduve.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserCharacterService {

    private final UserCharacterRepository userCharacterRepository;
    private final UserRepository userRepository;
    private final AllCharacterRepository allCharacterRepository;
    private final PreferenceRepository preferenceRepository;

    // userId로 캐릭터 설정 업데이트
    public UserCharacterPreferenceDto updateUserCharacter(Long userId, UserCharacterPreferenceRequestDto requestDto) {
        UserCharacter userCharacter = getUserCharacter(userId);

        if (requestDto.getUserCharacterName() != null) {
            userCharacter.setUserCharacterName(requestDto.getUserCharacterName());
        }

        // 기존 Preference 객체를 가져와서 업데이트
        Preference preference = userCharacter.getPreference();

        if (requestDto.getTone() != null) {
            // Tone 값이 Enum에 존재하는지 확인
            try {
                Tone toneEnum = Tone.valueOf(requestDto.getTone());  // ToneEnum을 enum으로 변환
                preference.setTone(toneEnum);  // 유효한 값이면 설정
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tone value");  // 유효하지 않으면 400 오류
            }
        }

        if (requestDto.getDescriptionLevel() != null) {
            // DescriptionLevel 값이 Enum에 존재하는지 확인
            try {
                DescriptionLevel descriptionLevelEnum = DescriptionLevel.valueOf(requestDto.getDescriptionLevel());  // DescriptionLevelEnum을 enum으로 변환
                preference.setDescriptionLevel(descriptionLevelEnum);  // 유효한 값이면 설정
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid descriptionlevel value");  // 유효하지 않으면 400 오류
            }
        }

        if (requestDto.getCharacterId() != null) {
            AllCharacter newCharacter = allCharacterRepository.findById(requestDto.getCharacterId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));
            userCharacter.setCharacter(newCharacter);  // 새로운 캐릭터로 변경
        }


        userCharacterRepository.save(userCharacter);
        return UserCharacterPreferenceDto.from(userCharacter);  // UserCharacter -> UserCharacterPreferenceDto
    }

    // userId로 캐릭터 설정 조회
    public UserCharacterPreferenceDto getUserCharacterPreference(Long userId) {
        UserCharacter userCharacter = getUserCharacter(userId);
        return UserCharacterPreferenceDto.from(userCharacter);  // UserCharacter -> UserCharacterPreferenceDto
    }

    public UserCharacter getUserCharacter(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("not found user"));

        return userCharacterRepository.findByUser(user)
                .orElseThrow(()-> new RuntimeException("not found userCharacter"));
    }

    // 사용자가 설정한 TONE, DISCRIPTIONLEVEL 조회
    public Preference getPrefernceByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("not found user"));
        UserCharacter userCharacter = userCharacterRepository.findByUser(user)
                .orElseThrow(()-> new RuntimeException("not found userCharacter"));
        return userCharacter.getPreference();
    }

    // 기본값을 설정하는 메서드
    public Preference createDefaultPreference() {
        Preference preference = new Preference();
        preference.setTone(Tone.FRIENDLY);
        preference.setDescriptionLevel(DescriptionLevel.HIGH);
        return preferenceRepository.save(preference);
    }

    // 기본값을 설정하는 메서드
    public UserCharacter createDefaultUserCharacter(User user, AllCharacter character) {
        UserCharacter userCharacter = new UserCharacter();
        userCharacter.setUser(user);
        // characterId 1인 캐릭터 가져와서 파라미터 넣어주면 될 듯
        userCharacter.setCharacter(character);
        userCharacter.setUserCharacterName(character.getCharacterName());
        // 기본 Preference 설정
        userCharacter.setPreference(createDefaultPreference());
        return userCharacterRepository.save(userCharacter);
    }
}
