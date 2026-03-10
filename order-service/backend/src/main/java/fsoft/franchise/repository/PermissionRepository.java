package fsoft.franchise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fsoft.franchise.entity.external.PermissionEntity;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
}
