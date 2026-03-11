package fsoft.franchise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fsoft.franchise.entity.external.AccountRoleEntity;

public interface AccountRoleRepository extends JpaRepository<AccountRoleEntity, AccountRoleEntity.AccountRoleId> {
}
