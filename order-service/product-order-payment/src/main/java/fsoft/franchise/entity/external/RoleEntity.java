package fsoft.franchise.entity.external;

import fsoft.franchise.infrastructure.BaseEntity;
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
@EqualsAndHashCode(callSuper = false)
public class RoleEntity extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // FRANCHISE_ADMIN | STORE_MANAGER | CUSTOMER

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "role", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AccountRoleEntity> userRoles;

    @OneToMany(mappedBy = "role", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RolePermissionEntity> rolePermissions;

}

