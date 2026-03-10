package fsoft.franchise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fsoft.franchise.entity.external.RoleEntity;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByCode(String code);
}
