package fsoft.franchise.auth.module.repository;

import fsoft.franchise.auth.module.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionEntity.RolePermissionId> {

    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
            "FROM RolePermissionEntity rp " +
            "WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    boolean existByRoleIdAndPermissionId(UUID roleId, UUID permissionId);


    boolean existsByRoleId(UUID roleId);

    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    List<RolePermissionEntity> findByRoleId(UUID roleId);

    void deleteByRoleId(UUID roleId);
}
