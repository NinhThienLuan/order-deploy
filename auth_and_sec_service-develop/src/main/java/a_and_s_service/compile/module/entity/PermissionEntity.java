package a_and_s_service.compile.module.entity;

import a_and_s_service.compile.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "permission")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PermissionEntity extends BaseEntity {

    @Column(name = "module", nullable = false, length = 50)
    private String module; // Ví dụ module như: ACCOUNT, ORDER, NOTIFICATION, v.v.
    @Column(name = "code", nullable = false, length = 100, unique = true)
    private String code;
    // Thì code sẽ có dạng: order:create, order:read, order:update, order:delete, account:create, account:read, v.v. (action:resource)

    @OneToMany(mappedBy = "permission")
    private List<RolePermissionEntity> rolePermissions;

}
