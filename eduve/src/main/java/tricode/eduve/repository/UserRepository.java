package tricode.eduve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tricode.eduve.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    //username을 받아 DB 테이블에서 회원을 조회하는 메소드 작성
    User findByUsername(String username);
}