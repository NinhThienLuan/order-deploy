package fsoft.franchise.repository;

import fsoft.franchise.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    /** Returns all active, non-deleted categories ordered by name. */
    List<CategoryEntity> findAllByActiveTrueAndDeleteAtIsNullOrderByNameAsc();
}
