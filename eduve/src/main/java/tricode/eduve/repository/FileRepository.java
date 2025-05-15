package tricode.eduve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tricode.eduve.domain.File;
import tricode.eduve.domain.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository <File, Long> {
    // 이름순 (오름차순)
    List<File> findAllByOrderByFileNameAsc();

    // 최신순 (createdAt 내림차순)
    List<File> findAllByOrderByCreatedTimeDesc();

    // 파일 이름으로 검색 (포함 검색, 대소문자 구분 없이)
    List<File> findByFileNameContainingIgnoreCase(String keyword);

    List<File> findByUserInAndFolderIsNull(List<User> accessibleUsers);

    Optional<File> findByFileName(String fileName);
}
