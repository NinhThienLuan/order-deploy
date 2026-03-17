package fsoft.franchise.auth.module.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "account_role")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AccountRoleEntity {

    @EmbeddedId
    @Builder.Default
    private AccountRoleId id = new AccountRoleId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountRoleId implements Serializable {
        private static final long serialVersionUID = 1L; // Đảm bảo tương thích khi serializable

        private UUID accountId;
        private UUID roleId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AccountRoleId that)) return false;
            return Objects.equals(accountId, that.accountId) &&
                   Objects.equals(roleId, that.roleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, roleId);
        }
    }
}
