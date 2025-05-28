package tricode.eduve.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tricode.eduve.domain.User;
import tricode.eduve.dto.response.User.CustomUserDetails;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // ============================== //
        // 요청 URI 가져오기
        String requestURI = request.getRequestURI();

        // `/join/student` 또는 `/join/teacher` 경로에서는 토큰 인증을 건너뜀
        if (requestURI.startsWith("/join/student") || requestURI.startsWith("/join/teacher") || requestURI.startsWith("/join/check-username") || requestURI.startsWith("/join/check-email") || requestURI.startsWith("/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        // =============================== //

        //request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        String token = authorization.split(" ")[1];
        System.out.println("Extracted Token: " + token);

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {

            System.out.println("token expired");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰에서 username, userId, role 획득
        String username = jwtUtil.getUsername(token);
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        System.out.println("Extracted Username: " + username);
        System.out.println("Extracted UserId: " + userId);
        System.out.println("Extracted Role: " + role);

        //userEntity를 생성하여 값 set
        User userEntity = new User();
        userEntity.setUsername(username);
        userEntity.setUserId(userId);
        userEntity.setPassword("temppassword");
        userEntity.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println("Authentication 저장 완료!");

        filterChain.doFilter(request, response);
    }
}
