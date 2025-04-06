package tricode.eduve.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // 클라이언트 측에서 JWT 토큰을 삭제하도록 유도
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}

//클라이언트에서 로그아웃 시, JWT 토큰을 로컬 스토리지나 세션 스토리지에서 삭제해야 함. Authorization 헤더에서 토큰을 제거하는 방법도 있음.
//
//로그아웃 시 클라이언트에서 해야 할 작업:
//1. 로컬 스토리지나 세션 스토리지에서 토큰을 삭제
//2. HTTP 요청 헤더에서 Authorization 토큰을 삭제
//3. 로그인 페이지로 리다이렉트
