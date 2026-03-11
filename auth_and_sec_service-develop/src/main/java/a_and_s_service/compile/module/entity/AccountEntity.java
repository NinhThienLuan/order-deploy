package a_and_s_service.compile.module.entity;

import a_and_s_service.compile.infrastructure.persistence.BaseEntity;
import a_and_s_service.compile.module.enumType.StatusEnum;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "accounts")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountEntity extends BaseEntity {

    @Column(name = "email", nullable = true, unique = true)
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "phone_number", nullable = true, unique = true)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    @NotEmpty
    @NotBlank(message = "Password must not be blank")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEnum status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProfileEntity profile;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AccountRoleEntity> accountRoles;
}
