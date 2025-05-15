package tricode.eduve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tricode.eduve.domain.AllCharacter;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllCharacterRepository extends JpaRepository<AllCharacter, Long> {
    Optional<AllCharacter> findByAllCharacterId(Long allCharacterId);
}
