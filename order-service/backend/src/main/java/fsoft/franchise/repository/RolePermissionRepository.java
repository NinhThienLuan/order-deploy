package fsoft.franchise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fsoft.franchise.entity.external.RolePermissionEntity;

public interface RolePermissionRepository
        extends JpaRepository<RolePermissionEntity, RolePermissionEntity.RolePermissionId> {
}
