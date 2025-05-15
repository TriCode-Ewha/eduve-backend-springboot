package tricode.eduve.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tricode.eduve.domain.User;
import tricode.eduve.dto.JoinDTO;
import tricode.eduve.dto.response.JoinResponseDto;
import tricode.eduve.repository.UserRepository;
import tricode.eduve.service.StudentJoinService;
import tricode.eduve.service.TeacherJoinService;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/join")
@RequiredArgsConstructor
public class JoinController {

    private final StudentJoinService studentJoinService;
    private final TeacherJoinService teacherJoinService;
    private final UserRepository userRepository;

    @PostMapping("/student")
    public ResponseEntity<JoinResponseDto> studentJoinProcess(@RequestBody JoinDTO joinDTO) {

        System.out.println(joinDTO.getUsername());
        User savedUser = studentJoinService.joinProcess(joinDTO);

        JoinResponseDto response = JoinResponseDto.builder()
                .joinDto(joinDTO)
                .userId(savedUser.getUserId())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/teacher")
    public ResponseEntity<JoinResponseDto> teacherJoinProcess(@RequestBody JoinDTO joinDTO) {

        System.out.println(joinDTO.getUsername());
        User savedUser = teacherJoinService.joinProcess(joinDTO);

        JoinResponseDto response = JoinResponseDto.builder()
                .joinDto(joinDTO)
                .userId(savedUser.getUserId())
                .build();

        return ResponseEntity.ok(response);
    }

    // 아이디 중복 확인
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean exists = userRepository.existsByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "이미 존재하는 아이디입니다." : "사용 가능한 아이디입니다.");
        return ResponseEntity.ok(response);
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "이미 존재하는 메일입니다." : "사용 가능한 메일입니다.");
        return ResponseEntity.ok(response);
    }
}
