package tricode.eduve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tricode.eduve.domain.Folder;
import tricode.eduve.domain.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByPath(String path);

    Optional<Folder> findById(Long id);

    List<Folder> findByUserInAndParentFolderIsNull(List<User> accessibleUsers);
}
