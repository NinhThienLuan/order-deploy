package fsoft.franchise.auth.module.entity;

import fsoft.franchise.auth.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RoleEntity extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // ví dụ: "ADMIN", "USER", "MANAGER"

    @Column(name = "name", nullable = false, length = 100)
    private String name; // ví dụ: "Quản trị viên", "Người dùng", "Quản lý"

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "role", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AccountRoleEntity> userRoles;

    @OneToMany(mappedBy = "role", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RolePermissionEntity> rolePermissions;

}
