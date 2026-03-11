package a_and_s_service.compile.module.repository;

import a_and_s_service.compile.module.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    RoleEntity findByCode(String code);
}
