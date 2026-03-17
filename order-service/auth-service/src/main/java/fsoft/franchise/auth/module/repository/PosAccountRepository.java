package fsoft.franchise.auth.module.repository;

import fsoft.franchise.auth.module.entity.PosAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PosAccountRepository extends JpaRepository<PosAccountEntity, UUID> {
}
