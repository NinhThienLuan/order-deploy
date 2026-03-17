package fsoft.franchise.auth.infrastructure.initializer;

import fsoft.franchise.auth.module.entity.*;
import fsoft.franchise.auth.module.enumType.GenderEnum;
import fsoft.franchise.auth.module.enumType.StatusEnum;
import fsoft.franchise.auth.module.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DataInit for auth-service — seeds accounts with fixed UUIDs that match
 * product-order-payment service.
 * This ensures both microservices recognize the same users.
 * <p>
 * UUID naming convention (copy pattern from order service):
 * Role UUIDs: 00000000-0000-0000-0001-xxxxxxxxxxxx
 * Account UUIDs: 00000000-0000-0000-0002-xxxxxxxxxxxx
 */
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
        private final FranchiseRepository franchiseRepository;
        private final PosAccountRepository posAccountRepository;

        // ─── Fixed UUIDs (must match product-order-payment service) ───────────────
        // Role UUIDs
        public static final UUID ROLE_ADMIN_ID = UUID.fromString("00000000-0000-0000-0001-000000000001");
        public static final UUID ROLE_MANAGER_ID = UUID.fromString("00000000-0000-0000-0001-000000000002");
        public static final UUID ROLE_USER_ID = UUID.fromString("00000000-0000-0000-0001-000000000003");
        public static final UUID ROLE_POS_ID = UUID.fromString("00000000-0000-0000-0001-000000000004");

        // Account UUIDs
        public static final UUID ACC_FRANCHISE_ADMIN_ID = UUID.fromString("00000000-0000-0000-0002-000000000001");
        public static final UUID ACC_MANAGER_HCM1_ID = UUID.fromString("00000000-0000-0000-0002-000000000002");
        public static final UUID ACC_MANAGER_HCM2_ID = UUID.fromString("00000000-0000-0000-0002-000000000003");
        public static final UUID ACC_CUSTOMER_1_ID = UUID.fromString("00000000-0000-0000-0002-000000000004");
        public static final UUID ACC_CUSTOMER_2_ID = UUID.fromString("00000000-0000-0000-0002-000000000005");
        public static final UUID ACC_CUSTOMER_3_ID = UUID.fromString("00000000-0000-0000-0002-000000000006");
        public static final UUID ACC_CUSTOMER_4_ID = UUID.fromString("00000000-0000-0000-0002-000000000007");
        public static final UUID ACC_POS_ID = UUID.fromString("00000000-0000-0000-0002-000000000008");

        // Franchise UUIDs (must match STORE_1_ID, STORE_2_ID in order service)
        public static final UUID FRANCHISE_HCM1_ID = UUID.fromString("00000000-0000-0000-0012-000000000001");
        public static final UUID FRANCHISE_HCM2_ID = UUID.fromString("00000000-0000-0000-0012-000000000002");

        @Override
        @Transactional
        public void run(String... args) {
                boolean fixedSeedExists =
                                roleRepository.existsById(ROLE_ADMIN_ID)
                                                && roleRepository.existsById(ROLE_MANAGER_ID)
                                                && roleRepository.existsById(ROLE_USER_ID)
                                                && roleRepository.existsById(ROLE_POS_ID)
                                                && accountRepository.existsById(ACC_FRANCHISE_ADMIN_ID)
                                                && accountRepository.existsById(ACC_MANAGER_HCM1_ID)
                                                && accountRepository.existsById(ACC_MANAGER_HCM2_ID)
                                                && accountRepository.existsById(ACC_CUSTOMER_1_ID)
                                                && accountRepository.existsById(ACC_CUSTOMER_2_ID)
                                                && accountRepository.existsById(ACC_CUSTOMER_3_ID)
                                                && accountRepository.existsById(ACC_CUSTOMER_4_ID)
                                                && accountRepository.existsById(ACC_POS_ID)
                                                && franchiseRepository.existsById(FRANCHISE_HCM1_ID);

                if (fixedSeedExists) {
                        return;
                }

                // Existing data may contain random UUIDs from an older seed.
                // Reset auth seed tables so fixed UUIDs can be applied deterministically.
                if (roleRepository.count() > 0 || accountRepository.count() > 0) {
                        posAccountRepository.deleteAllInBatch();
                        franchiseRepository.deleteAllInBatch();
                        accountRoleRepository.deleteAllInBatch();
                        rolePermissionRepository.deleteAllInBatch();
                        profileRepository.deleteAllInBatch();
                        accountRepository.deleteAllInBatch();
                        roleRepository.deleteAllInBatch();
                        permissionRepository.deleteAllInBatch();
                }

                // ===== 1. PERMISSIONS =====
                PermissionEntity pProductRead = permissionRepository
                                .save(PermissionEntity.builder().module("Product").code("product:read").build());
                PermissionEntity pProductWrite = permissionRepository
                                .save(PermissionEntity.builder().module("Product").code("product:write").build());
                PermissionEntity pProductDelete = permissionRepository
                                .save(PermissionEntity.builder().module("Product").code("product:delete").build());
                PermissionEntity pOrderRead = permissionRepository
                                .save(PermissionEntity.builder().module("Order").code("order:read").build());
                PermissionEntity pOrderWrite = permissionRepository
                                .save(PermissionEntity.builder().module("Order").code("order:write").build());
                PermissionEntity pOrderDelete = permissionRepository
                                .save(PermissionEntity.builder().module("Order").code("order:delete").build());
                PermissionEntity pAccountRead = permissionRepository
                                .save(PermissionEntity.builder().module("Account").code("account:read").build());
                PermissionEntity pAccountWrite = permissionRepository
                                .save(PermissionEntity.builder().module("Account").code("account:write").build());
                PermissionEntity pAccountDelete = permissionRepository
                                .save(PermissionEntity.builder().module("Account").code("account:delete").build());
                PermissionEntity pReportView = permissionRepository
                                .save(PermissionEntity.builder().module("Report").code("report:view").build());

                // ===== 2. ROLES =====
                RoleEntity adminRole = roleRepository.save(RoleEntity.builder().id(ROLE_ADMIN_ID).code("ADMIN")
                                .name("Quản trị viên").description("Toàn quyền hệ thống").build());
                RoleEntity managerRole = roleRepository.save(RoleEntity.builder().id(ROLE_MANAGER_ID).code("MANAGER")
                                .name("Quản lý").description("Quản lý đơn hàng và sản phẩm").build());
                RoleEntity userRole = roleRepository.save(RoleEntity.builder().id(ROLE_USER_ID).code("USER")
                                .name("Người dùng").description("Quyền xem cơ bản").build());
                RoleEntity posRole = roleRepository.save(RoleEntity.builder().id(ROLE_POS_ID).code("POS")
                                .name("POS System").description("Point of Sale terminal").build());

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
                                RolePermissionEntity.builder().role(adminRole).permission(pReportView).build()));
                // MANAGER: quản lý product + order + xem report
                rolePermissionRepository.saveAll(List.of(
                                RolePermissionEntity.builder().role(managerRole).permission(pProductRead).build(),
                                RolePermissionEntity.builder().role(managerRole).permission(pProductWrite).build(),
                                RolePermissionEntity.builder().role(managerRole).permission(pOrderRead).build(),
                                RolePermissionEntity.builder().role(managerRole).permission(pOrderWrite).build(),
                                RolePermissionEntity.builder().role(managerRole).permission(pReportView).build()));
                // USER: xem và tạo đơn hàng
                rolePermissionRepository.saveAll(List.of(
                                RolePermissionEntity.builder().role(userRole).permission(pProductRead).build(),
                                RolePermissionEntity.builder().role(userRole).permission(pOrderRead).build(),
                                RolePermissionEntity.builder().role(userRole).permission(pOrderWrite).build()));
                // POS: quản lý đơn hàng và xem report
                rolePermissionRepository.saveAll(List.of(
                                RolePermissionEntity.builder().role(posRole).permission(pProductRead).build(),
                                RolePermissionEntity.builder().role(posRole).permission(pOrderRead).build(),
                                RolePermissionEntity.builder().role(posRole).permission(pOrderWrite).build(),
                                RolePermissionEntity.builder().role(posRole).permission(pReportView).build()));

                // ===== 4. FRANCHISES =====
                FranchiseEntity hcm1 = franchiseRepository.save(FranchiseEntity.builder()
                                .id(FRANCHISE_HCM1_ID).name("E-Coffee HCM District 1").status(StatusEnum.ACTIVE).build());
                FranchiseEntity hcm2 = franchiseRepository.save(FranchiseEntity.builder()
                                .id(FRANCHISE_HCM2_ID).name("E-Coffee HCM District 7").status(StatusEnum.ACTIVE).build());

                // ===== 5. ACCOUNTS + PROFILES (with fixed UUIDs matching
                // product-order-payment) =====
                String defaultPass = passwordEncoder.encode("Password@123");

                // Admin account
                AccountEntity admin = buildAccount(ACC_FRANCHISE_ADMIN_ID, "admin@capitalfranchise.vn", "0900000001",
                                defaultPass);
                // Manager accounts
                AccountEntity manager1 = buildAccount(ACC_MANAGER_HCM1_ID, "manager.hcm1@capitalfranchise.vn",
                                "0900000002", defaultPass);
                AccountEntity manager2 = buildAccount(ACC_MANAGER_HCM2_ID, "manager.hcm2@capitalfranchise.vn",
                                "0900000003", defaultPass);
                // Customer accounts (matching product-order-payment)
                AccountEntity customer1 = buildAccount(ACC_CUSTOMER_1_ID, "alice@gmail.com", "0911000001", defaultPass);
                AccountEntity customer2 = buildAccount(ACC_CUSTOMER_2_ID, "bob@gmail.com", "0911000002", defaultPass);
                AccountEntity customer3 = buildAccount(ACC_CUSTOMER_3_ID, "charlie@gmail.com", "0911000003",
                                defaultPass);
                AccountEntity customer4 = buildAccount(ACC_CUSTOMER_4_ID, "diana@gmail.com", "0911000004", defaultPass);
                // POS account (for Store 1)
                AccountEntity pos = buildAccount(ACC_POS_ID, "pos@gmail.com", "0900000099", defaultPass);

                // Save accounts and keep managed references for later relations.
                admin = accountRepository.save(admin);
                manager1 = accountRepository.save(manager1);
                manager2 = accountRepository.save(manager2);
                customer1 = accountRepository.save(customer1);
                customer2 = accountRepository.save(customer2);
                customer3 = accountRepository.save(customer3);
                customer4 = accountRepository.save(customer4);
                pos = accountRepository.save(pos);

                // Profiles
                profileRepository.saveAll(List.of(
                                buildProfile(admin, "Capital", "Admin", GenderEnum.MALE, LocalDate.of(1985, 1, 15)),
                                buildProfile(manager1, "Nguyen", "Minh Quan", GenderEnum.MALE,
                                                LocalDate.of(1990, 5, 20)),
                                buildProfile(manager2, "Tran", "Thanh Huong", GenderEnum.FEMALE,
                                                LocalDate.of(1992, 8, 10)),
                                buildProfile(customer1, "Alice", "Nguyen", GenderEnum.FEMALE,
                                                LocalDate.of(1998, 3, 12)),
                                buildProfile(customer2, "Bob", "Tran", GenderEnum.MALE, LocalDate.of(2000, 7, 25)),
                                buildProfile(customer3, "Charlie", "Le", GenderEnum.MALE, LocalDate.of(1995, 11, 5)),
                                buildProfile(customer4, "Diana", "Pham", GenderEnum.FEMALE, LocalDate.of(1997, 6, 18)),
                                buildProfile(pos, "System", "POS", GenderEnum.MALE, LocalDate.of(2024, 1, 1))));

                // ===== 6. ACCOUNT - ROLE =====
                accountRoleRepository.saveAll(List.of(
                                AccountRoleEntity.builder().account(admin).role(adminRole).build(),
                                AccountRoleEntity.builder().account(manager1).role(managerRole).build(),
                                AccountRoleEntity.builder().account(manager2).role(managerRole).build(),
                                AccountRoleEntity.builder().account(customer1).role(userRole).build(),
                                AccountRoleEntity.builder().account(customer2).role(userRole).build(),
                                AccountRoleEntity.builder().account(customer3).role(userRole).build(),
                                AccountRoleEntity.builder().account(customer4).role(userRole).build(),
                                AccountRoleEntity.builder().account(pos).role(posRole).build()));

                // ===== 7. POS - FRANCHISE LINK =====
                posAccountRepository.saveAll(List.of(
                                PosAccountEntity.builder().account(pos).franchise(hcm1).isActive(true).build(),
                                PosAccountEntity.builder().account(manager1).franchise(hcm1).isActive(true).build(),
                                PosAccountEntity.builder().account(manager2).franchise(hcm2).isActive(true).build()
                ));

                System.out.println(
                                ">>> DataInit: khởi tạo thành công 10 permissions, 4 roles, 2 franchises, 8 accounts (including POS and Managers linked to franchises)");
        }

        // ===== HELPERS =====
        private AccountEntity buildAccount(UUID id, String email, String phone, String password) {
                return buildAccount(id, email, phone, password, StatusEnum.ACTIVE);
        }

        private AccountEntity buildAccount(UUID id, String email, String phone, String password, StatusEnum status) {
                return AccountEntity.builder()
                                .id(id)
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