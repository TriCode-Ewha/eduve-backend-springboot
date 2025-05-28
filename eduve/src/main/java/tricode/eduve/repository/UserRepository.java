package tricode.eduve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tricode.eduve.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    //username을 받아 DB 테이블에서 회원을 조회하는 메소드 작성 (User 타입)
    User findByUsername(String username);
    Optional<User> findByUserId(Long userId);
    // username과 role을 받아 DB 테이블에서 회원을 조회
    Optional<User> findByUsernameAndRole(String username, String role);

}