package tricode.eduve.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tricode.eduve.domain.AllCharacter;
import tricode.eduve.domain.Preference;
import tricode.eduve.domain.User;
import tricode.eduve.domain.UserCharacter;
import tricode.eduve.dto.JoinDTO;
import tricode.eduve.repository.AllCharacterRepository;
import tricode.eduve.repository.PreferenceRepository;
import tricode.eduve.repository.UserCharacterRepository;
import tricode.eduve.repository.UserRepository;

@Service
public class TeacherJoinService {

    private final UserRepository userRepository;
    private final UserCharacterRepository userCharacterRepository;
    private final AllCharacterRepository allCharacterRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public TeacherJoinService(UserRepository userRepository, UserCharacterRepository userCharacterRepository,
                              AllCharacterRepository allCharacterRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.userRepository = userRepository;
        this.userCharacterRepository = userCharacterRepository;
        this.allCharacterRepository = allCharacterRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public User joinProcess(JoinDTO joinDTO) {

        System.out.println("회원가입 진입");
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String name = joinDTO.getName();
        String email = joinDTO.getEmail();

        Boolean isExistUsername = userRepository.existsByUsername(username);
        Boolean isExistEmail = userRepository.existsByEmail(email);

        if (isExistUsername || isExistEmail) {
            throw new RuntimeException("이미 존재하는 사용자명 또는 이메일입니다.");
        }

        User data = new User();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setName(name);
        data.setEmail(email);
        data.setRole("ROLE_Teacher");

        User savedUser = userRepository.save(data);
        System.out.println("Saved user: " + savedUser);

        userRepository.flush();
        System.out.println("flush 후 저장 시도됨");

        return savedUser;

/*
        // 기본 캐릭터 가져오기 (characterId = 1)
        AllCharacter defaultCharacter = allCharacterRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("기본 캐릭터를 찾을 수 없습니다."));

        // 기본 UserCharacter 생성 및 저장
        UserCharacter defaultUserCharacter = UserCharacter.createDefaultUserCharacter(savedUser, defaultCharacter);
        userCharacterRepository.save(defaultUserCharacter);

        System.out.println("UserCharacter 저장됨: " + defaultUserCharacter);
*/

    }
}
