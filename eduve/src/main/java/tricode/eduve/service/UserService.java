package tricode.eduve.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tricode.eduve.domain.User;
import tricode.eduve.dto.request.User.UpdateUserDto;
import tricode.eduve.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 유저 조회
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 유저 이름 수정
    @Transactional
    public void updateUser(Long userId, UpdateUserDto dto) {
        User user = findById(userId);
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        // JPA 변경 감지 자동 업데이트
    }

    // 유저 삭제
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

}
