package tricode.eduve.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tricode.eduve.domain.User;
import tricode.eduve.dto.UpdateUserDto;
import tricode.eduve.dto.response.UserResponseDto;
import tricode.eduve.repository.UserRepository;
import tricode.eduve.service.UserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    public final UserService userService;

    // 유저 이름 수정
    @PatchMapping("/{userId}")
    public UserResponseDto updateUser(@PathVariable Long  userId, @RequestBody UpdateUserDto updateDto) {
        userService.updateUser(userId, updateDto);
        User updatedUser = userService.findById(userId);
        return UserResponseDto.from(updatedUser);
    }

    // 유저 정보 조회
    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable Long userId) {
        User user = userService.findById(userId);
        return UserResponseDto.from(user);
    }

    // 유저 탈퇴
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long  userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("유저가 삭제되었습니다.");
    }
}
