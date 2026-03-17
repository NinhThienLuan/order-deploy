package fsoft.franchise.auth.module.repository;

import fsoft.franchise.auth.module.entity.AccountRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRoleEntity, AccountRoleEntity.AccountRoleId> {
    List<AccountRoleEntity> findByRoleId(UUID roleId);
}
