package tricode.eduve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tricode.eduve.domain.MessageLikePreference;
import tricode.eduve.domain.User;

import java.util.Optional;

public interface MessageLikePreferenceRepository extends JpaRepository<MessageLikePreference, Long> {
    Optional<MessageLikePreference> findByUser(User user);
}
