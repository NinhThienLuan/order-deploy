package fsoft.franchise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fsoft.franchise.entity.external.ProfileEntity;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
}
