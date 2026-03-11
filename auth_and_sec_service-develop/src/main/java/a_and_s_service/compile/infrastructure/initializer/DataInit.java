package a_and_s_service.compile.infrastructure.initializer;

import a_and_s_service.compile.module.entity.*;
import a_and_s_service.compile.module.enumType.GenderEnum;
import a_and_s_service.compile.module.enumType.StatusEnum;
import a_and_s_service.compile.module.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() > 0) return;

        // ===== 1. PERMISSIONS =====
        PermissionEntity pProductRead   = PermissionEntity.builder().module("Product").code("product:read").build();
        PermissionEntity pProductWrite  = PermissionEntity.builder().module("Product").code("product:write").build();
        PermissionEntity pProductDelete = PermissionEntity.builder().module("Product").code("product:delete").build();
        PermissionEntity pOrderRead     = PermissionEntity.builder().module("Order").code("order:read").build();
        PermissionEntity pOrderWrite    = PermissionEntity.builder().module("Order").code("order:write").build();
        PermissionEntity pOrderDelete   = PermissionEntity.builder().module("Order").code("order:delete").build();
        PermissionEntity pAccountRead   = PermissionEntity.builder().module("Account").code("account:read").build();
        PermissionEntity pAccountWrite  = PermissionEntity.builder().module("Account").code("account:write").build();
        PermissionEntity pAccountDelete = PermissionEntity.builder().module("Account").code("account:delete").build();
        PermissionEntity pReportView    = PermissionEntity.builder().module("Report").code("report:view").build();
        permissionRepository.saveAll(List.of(
                pProductRead, pProductWrite, pProductDelete,
                pOrderRead, pOrderWrite, pOrderDelete,
                pAccountRead, pAccountWrite, pAccountDelete,
                pReportView
        ));

        // ===== 2. ROLES =====
        RoleEntity adminRole   = RoleEntity.builder().code("ADMIN").name("Quản trị viên").description("Toàn quyền hệ thống").build();
        RoleEntity managerRole = RoleEntity.builder().code("MANAGER").name("Quản lý").description("Quản lý đơn hàng và sản phẩm").build();
        RoleEntity userRole    = RoleEntity.builder().code("USER").name("Người dùng").description("Quyền xem cơ bản").build();
        roleRepository.saveAll(List.of(adminRole, managerRole, userRole));

        // ===== 3. ROLE - PERMISSION =====
        // ADMIN: toàn quyền
        rolePermissionRepository.saveAll(List.of(
                RolePermissionEntity.builder().role(adminRole).permission(pProductRead).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pProductWrite).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pProductDelete).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pOrderRead).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pOrderWrite).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pOrderDelete).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pAccountRead).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pAccountWrite).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pAccountDelete).build(),
                RolePermissionEntity.builder().role(adminRole).permission(pReportView).build()
        ));
        // MANAGER: quản lý product + order + xem report
        rolePermissionRepository.saveAll(List.of(
                RolePermissionEntity.builder().role(managerRole).permission(pProductRead).build(),
                RolePermissionEntity.builder().role(managerRole).permission(pProductWrite).build(),
                RolePermissionEntity.builder().role(managerRole).permission(pOrderRead).build(),
                RolePermissionEntity.builder().role(managerRole).permission(pOrderWrite).build(),
                RolePermissionEntity.builder().role(managerRole).permission(pReportView).build()
        ));
        // USER: chỉ xem
        rolePermissionRepository.saveAll(List.of(
                RolePermissionEntity.builder().role(userRole).permission(pProductRead).build(),
                RolePermissionEntity.builder().role(userRole).permission(pOrderRead).build()
        ));

        // ===== 4. ACCOUNTS + PROFILES =====
        String defaultPass = passwordEncoder.encode("123");

        // Helper: tạo account + profile cùng lúc
        AccountEntity admin    = buildAccount("admin@gmail.com",       "0900000001", defaultPass);
        AccountEntity manager1 = buildAccount("minh.nguyen@gmail.com", "0900000002", defaultPass);
        AccountEntity manager2 = buildAccount("lan.tran@gmail.com",    "0900000003", defaultPass);
        AccountEntity user1    = buildAccount("hung.le@gmail.com",     "0900000004", defaultPass);
        AccountEntity user2    = buildAccount("hoa.pham@gmail.com",    "0900000005", defaultPass);
        AccountEntity user3    = buildAccount("tuan.vu@gmail.com",     "0900000006", defaultPass);
        AccountEntity user4    = buildAccount("mai.dang@gmail.com",    "0900000007", defaultPass);
        AccountEntity user5    = buildAccount("khoa.bui@gmail.com",    "0900000008", defaultPass);
        AccountEntity user6    = buildAccount("linh.hoang@gmail.com",  "0900000009", defaultPass, StatusEnum.INACTIVE);
        AccountEntity user7    = buildAccount("duc.dinh@gmail.com",    "0900000010", defaultPass, StatusEnum.INACTIVE);
        accountRepository.saveAll(List.of(admin, manager1, manager2, user1, user2, user3, user4, user5, user6, user7));

        // Profiles
        profileRepository.saveAll(List.of(
                buildProfile(admin,    "Admin",  "System",  GenderEnum.MALE,   LocalDate.of(1990, 1,  1)),
                buildProfile(manager1, "Minh",   "Nguyễn",  GenderEnum.MALE,   LocalDate.of(1992, 5, 15)),
                buildProfile(manager2, "Lan",    "Trần",    GenderEnum.FEMALE, LocalDate.of(1993, 8, 20)),
                buildProfile(user1,    "Hùng",   "Lê",      GenderEnum.MALE,   LocalDate.of(1995, 3, 10)),
                buildProfile(user2,    "Hoa",    "Phạm",    GenderEnum.FEMALE, LocalDate.of(1997, 7, 25)),
                buildProfile(user3,    "Tuấn",   "Vũ",      GenderEnum.MALE,   LocalDate.of(1998, 11, 5)),
                buildProfile(user4,    "Mai",    "Đặng",    GenderEnum.FEMALE, LocalDate.of(1999, 2, 14)),
                buildProfile(user5,    "Khoa",   "Bùi",     GenderEnum.MALE,   LocalDate.of(2000, 6, 30)),
                buildProfile(user6,    "Linh",   "Hoàng",   GenderEnum.FEMALE, LocalDate.of(2001, 9, 18)),
                buildProfile(user7,    "Đức",    "Đinh",    GenderEnum.MALE,   LocalDate.of(2002, 12, 3))
        ));

        // ===== 5. ACCOUNT - ROLE =====
        accountRoleRepository.saveAll(List.of(
                AccountRoleEntity.builder().account(admin).role(adminRole).build(),
                AccountRoleEntity.builder().account(manager1).role(managerRole).build(),
                AccountRoleEntity.builder().account(manager2).role(managerRole).build(),
                AccountRoleEntity.builder().account(user1).role(userRole).build(),
                AccountRoleEntity.builder().account(user2).role(userRole).build(),
                AccountRoleEntity.builder().account(user3).role(userRole).build(),
                AccountRoleEntity.builder().account(user4).role(userRole).build(),
                AccountRoleEntity.builder().account(user5).role(userRole).build(),
                AccountRoleEntity.builder().account(user6).role(userRole).build(),
                AccountRoleEntity.builder().account(user7).role(userRole).build()
        ));

        System.out.println(">>> DataInit: khởi tạo thành công 10 permissions, 3 roles, 10 accounts");
    }

    // ===== HELPERS =====
    private AccountEntity buildAccount(String email, String phone, String password) {
        return buildAccount(email, phone, password, StatusEnum.ACTIVE);
    }

    private AccountEntity buildAccount(String email, String phone, String password, StatusEnum status) {
        return AccountEntity.builder()
                .email(email)
                .phoneNumber(phone)
                .password(password)
                .status(status)
                .build();
    }

    private ProfileEntity buildProfile(AccountEntity account, String firstName, String lastName,
                                       GenderEnum gender, LocalDate birthDate) {
        return ProfileEntity.builder()
                .account(account)
                .firstName(firstName)
                .lastName(lastName)
                .gender(gender)
                .birthDate(birthDate)
                .build();
    }
}