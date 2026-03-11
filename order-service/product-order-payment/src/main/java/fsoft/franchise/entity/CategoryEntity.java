package fsoft.franchise.entity;

import fsoft.franchise.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "category")
public class CategoryEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Self-referencing FK
    @Column(name = "parent_id")
    private UUID parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private CategoryEntity parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<CategoryEntity> children;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "delete_at")
    private LocalDateTime deleteAt;
}
