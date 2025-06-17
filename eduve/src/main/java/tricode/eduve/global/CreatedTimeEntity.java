package tricode.eduve.global;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass // JPA Entity 클래스들이 CreatedTimeEntity를 상속할 경우, 필드들이 컬럼으로 인식되는 기능
@EntityListeners(AuditingEntityListener.class) // createdTime Auditing 기능
public abstract class CreatedTimeEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdTime;
}
