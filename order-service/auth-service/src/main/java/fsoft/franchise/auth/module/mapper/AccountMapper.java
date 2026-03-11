package fsoft.franchise.auth.module.mapper;

import fsoft.franchise.auth.module.dto.account.request.RegisterRequestDTO;
import fsoft.franchise.auth.module.dto.account.request.AdminCreateAccountRequestDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForAdminDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForUserDTO;
import fsoft.franchise.auth.module.dto.permission.response.PermissionResponseDTO;
import fsoft.franchise.auth.module.dto.permission.response.PermissionResponseDTO;
import fsoft.franchise.auth.module.dto.role.response.RoleResponseDTO;
import fsoft.franchise.auth.module.entity.AccountEntity;
import fsoft.franchise.auth.module.entity.AccountRoleEntity;
import fsoft.franchise.auth.module.entity.ProfileEntity;
import fsoft.franchise.auth.module.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    // ==========================================
    // 1. MAPPING RESPONSE (Trả data về FE) - Của bro viết đã quá chuẩn
    // ==========================================
    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName", source = "profile.lastName")
    @Mapping(target = "gender", source = "profile.gender")
    @Mapping(target = "birthDate", source = "profile.birthDate")
    @Mapping(target = "roles", source = "accountRoles", qualifiedByName = "accountRolesToRoleResponseDTOs")
    AccountResponseForAdminDTO toResponseDTO(AccountEntity accountEntity);

    // Map sang DTO dành cho User (không có roles)
    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName", source = "profile.lastName")
    @Mapping(target = "gender", source = "profile.gender")
    @Mapping(target = "birthDate", source = "profile.birthDate")
    AccountResponseForUserDTO toUserResponseDTO(AccountEntity accountEntity);

    // ==========================================
    // 2. MAPPING REQUEST
    // ==========================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Bỏ qua pass để Service tự mã hóa
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "accountRoles", ignore = true) // Quyền được Service tự động gán mặc định
    @Mapping(target = "deletedAt", ignore = true)
    AccountEntity registerToAccountEntity(RegisterRequestDTO request);

    // Tiện tay nhờ MapStruct map luôn data cho bảng Profile
    @Mapping(target = "userId", ignore = true) // ID sẽ do Service set từ Account truyền sang
    ProfileEntity registerToProfileEntity(RegisterRequestDTO request);


    // ==========================================
    // 3. MAPPING REQUEST - Dành cho luồng ADMIN TẠO TÀI KHOẢN
    // ==========================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Bỏ qua pass để Service tự mã hóa
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "accountRoles", ignore = true) // Quyền sẽ do Service móc từ list `roleIds` ra gán
    @Mapping(target = "deletedAt", ignore = true)
    AccountEntity adminCreateToAccountEntity(AdminCreateAccountRequestDTO request);

    // Profile cho luồng Admin
    @Mapping(target = "userId", ignore = true)
    ProfileEntity adminCreateToProfileEntity(AdminCreateAccountRequestDTO request);


    // ==========================================
    // 4. CUSTOM METHODS (Giữ nguyên của bro, rất xịn)
    // ==========================================
    @Named("accountRolesToRoleResponseDTOs")
    default List<RoleResponseDTO> accountRolesToRoleResponseDTOs(Set<AccountRoleEntity> accountRoles) {
        if (accountRoles == null) return null;
        return accountRoles.stream()
                .map(ar -> roleToRoleResponseDTO(ar.getRole()))
                .toList();
    }

    default RoleResponseDTO roleToRoleResponseDTO(RoleEntity role) {
        if (role == null) return null;
        List<PermissionResponseDTO> permissions = role.getRolePermissions() == null ? null :
                role.getRolePermissions().stream()
                        .map(rp -> new PermissionResponseDTO(
                                rp.getPermission().getId(),
                                rp.getPermission().getModule(),
                                rp.getPermission().getCode()
                        ))
                        .toList();
        return new RoleResponseDTO(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                permissions
        );
    }
}