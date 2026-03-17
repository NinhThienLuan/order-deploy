package fsoft.franchise.infrastructure;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true; // Mặc định là mới

    @Override
    public boolean isNew() {
        return isNew || id == null;
    }

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = UUID.randomUUID(); // auto-generate only when no ID is set manually
        }
    }

    @PostPersist
    @PostLoad
    protected void setNotNew() {
        this.isNew = false;
    }
}
