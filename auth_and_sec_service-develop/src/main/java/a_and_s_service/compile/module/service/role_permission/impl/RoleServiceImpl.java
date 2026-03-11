package a_and_s_service.compile.module.service.role_permission.impl;

import a_and_s_service.compile.common.exception.ApiException;
import a_and_s_service.compile.common.exception.ErrorCode;
import a_and_s_service.compile.module.dto.role.request.RoleRequestDTO;
import a_and_s_service.compile.module.dto.role.request.RoleUpdateRequestDTO;
import a_and_s_service.compile.module.dto.role.response.RoleResponseDTO;
import a_and_s_service.compile.module.entity.AccountRoleEntity;
import a_and_s_service.compile.module.entity.PermissionEntity;
import a_and_s_service.compile.module.entity.RoleEntity;
import a_and_s_service.compile.module.entity.RolePermissionEntity;
import a_and_s_service.compile.module.mapper.RoleMapper;
import a_and_s_service.compile.module.repository.RoleRepository;
import a_and_s_service.compile.module.service.account_role.AccountRoleService;
import a_and_s_service.compile.module.service.role_permission.PermissionService;
import a_and_s_service.compile.module.service.role_permission.RolePermissionService;
import a_and_s_service.compile.module.service.role_permission.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.security.Permission;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final AccountRoleService accountRoleService;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;
    private final RolePermissionService rolePermissionService;

    @Override
    public RoleResponseDTO insertRole(RoleRequestDTO roleRequestDTO) {
        if(roleRequestDTO == null) {
            log.error("roleRequestDTO is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role request data cannot be null");
        }

        RoleEntity role = roleMapper.toRoleEntity(roleRequestDTO);
        RoleEntity savedRole = roleRepository.save(role);

        List<PermissionEntity> permissionEntityList = permissionService.getAllPermissionEntityByIds(roleRequestDTO.permissionIds());

        if(permissionEntityList.size() != roleRequestDTO.permissionIds().size()) {
            log.error("Some permissions not found for IDs: {}", roleRequestDTO.permissionIds());
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Some permissions not found for the provided IDs");
        }

        Set<RolePermissionEntity> rolePermissions = permissionEntityList.stream()
                .map(permission -> RolePermissionEntity.builder()
                        .role(savedRole)
                        .permission(permission)
                        .build()
                ).collect(Collectors.toSet());


        rolePermissionService.saveAllRolePermission(rolePermissions);
        savedRole.setRolePermissions(rolePermissions);

        return roleMapper.toRoleResponseDTO(savedRole);
    }

    @Override
    public RoleResponseDTO updateRole(UUID roleId, RoleUpdateRequestDTO roleRequestDTO) {
        if (roleId == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role ID cannot be null");
        }

        RoleEntity existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found"));

        // FIX Ở ĐÂY: Chỉ update nếu FE có gửi lên
        // ==========================================
        if (roleRequestDTO.name() != null && !roleRequestDTO.name().trim().isEmpty()) {
            existingRole.setName(roleRequestDTO.name());
        }

        if (roleRequestDTO.description() != null) {
            existingRole.setDescription(roleRequestDTO.description());
        }


//        Xử lý bảng trung gian role-permission
        if (roleRequestDTO.permissionIds() != null){
//            Xóa hết các role-permission cũ ở bảng trung gian
            // 1. Lấy lên list permission mới từ request
            List<PermissionEntity> permissionEntityList = permissionService.getAllPermissionEntityByIds(roleRequestDTO.permissionIds());

            if(permissionEntityList.size() != roleRequestDTO.permissionIds().size()) {
                log.error("Some permissions not found for IDs: {}", roleRequestDTO.permissionIds());
                throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Some permissions not found for the provided IDs");
            }

            // 2. CLEAR RỔ CŨ (Để orphanRemoval tự động sinh lệnh DELETE)
            if (existingRole.getRolePermissions() != null) {
                existingRole.getRolePermissions().clear();
            }

//            Nạp lại các role-permission mới vào bảng trung gian
            Set<RolePermissionEntity> rolePermissions = permissionEntityList.stream()
                    .map(permission -> RolePermissionEntity.builder()
                            .role(existingRole)
                            .permission(permission)
                            .build()
                    ).collect(Collectors.toSet());

            // 4. ADD VÀO LẠI (Nạp đệ mới vào rổ)
            if (existingRole.getRolePermissions() == null) {
                existingRole.setRolePermissions(rolePermissions);
            }
            else {
                existingRole.getRolePermissions().addAll(rolePermissions);
            }
        }
        RoleEntity savedRole = roleRepository.save(existingRole);
        return roleMapper.toRoleResponseDTO(savedRole);
    }

    @Override
    public void deleteRole(UUID roleId) {
        if (roleId == null){
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role ID cannot be null");
        }
        List<AccountRoleEntity> accountRoles = accountRoleService.findByRoleId(roleId);
        if (!accountRoles.isEmpty()) {
            log.error("Cannot delete role with ID {} because it is assigned to {} accounts", roleId, accountRoles.size());
            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Cannot delete role because it is assigned to one or more accounts");
        }

        roleRepository.deleteById(roleId);
    }

    @Override
    public RoleResponseDTO getRoleById(UUID roleId) {
        if (roleId == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role ID cannot be null");
        }
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found with id: " + roleId));
        return roleMapper.toRoleResponseDTO(role);
    }

    @Override
    public RoleEntity getRoleByIdEntity(UUID roleId) {
        if (roleId == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role ID cannot be null");
        }
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found with id: " + roleId));
    }

    @Override
    public RoleEntity getRoleByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role code cannot be null or blank");
        }
        RoleEntity role = roleRepository.findByCode((code));
        if (role == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found with code: " + code);
        }
        return role;
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        List<RoleEntity> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleMapper::toRoleResponseDTO)
                .toList();
    }

    @Override
    public List<RoleEntity> getAllRoleByIds(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role IDs cannot be null or empty");
        }
        return roleRepository.findAllById(roleIds);
    }

    private void checkRoleRequestDTO(RoleRequestDTO roleRequestDTO) {
        // Validate role name
        if (roleRequestDTO.name() == null || roleRequestDTO.name().isBlank()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Role name cannot be null or blank");
        }
        // Validate permissions list
        if (roleRequestDTO.code() == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Code cannot be null or blank");
        }
    }
}
