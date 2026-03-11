package a_and_s_service.compile.module.repository;

import a_and_s_service.compile.module.entity.AccountEntity;
import a_and_s_service.compile.module.enumType.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByEmailAndStatus(String email, StatusEnum status);

    Optional<AccountEntity> findByEmail(String email);

    Optional<AccountEntity> findByPhoneNumberAndStatus(String phoneNumber, StatusEnum status);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
           "FROM AccountRoleEntity ar " +
           "WHERE ar.account.id = :accountId AND ar.role.id = :roleId")
    boolean existsByAccountAndRoleId(@Param("accountId") UUID accountId, @Param("roleId") UUID roleId);

    // ===== Single account with full details =====
    @Query("SELECT a FROM AccountEntity a " +
           "LEFT JOIN FETCH a.profile " +
           "LEFT JOIN FETCH a.accountRoles ar " +
           "LEFT JOIN FETCH ar.role r " +
           "LEFT JOIN FETCH r.rolePermissions rp " +
           "LEFT JOIN FETCH rp.permission " +
           "WHERE a.id = :id")
    Optional<AccountEntity> findByIdWithDetails(@Param("id") UUID id);

    // AccountRepository.java - thêm query có pagination
    @Query(value = "SELECT DISTINCT a FROM AccountEntity a " +
            "LEFT JOIN FETCH a.profile " +
            "LEFT JOIN FETCH a.accountRoles ar " +
            "LEFT JOIN FETCH ar.role r " +
            "LEFT JOIN FETCH r.rolePermissions rp " +
            "LEFT JOIN FETCH rp.permission",
            countQuery = "SELECT COUNT(DISTINCT a) FROM AccountEntity a")
    Page<AccountEntity> findAllWithDetails(Pageable pageable);

}
