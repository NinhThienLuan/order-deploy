package a_and_s_service.compile.module.service.role_permission;

import a_and_s_service.compile.module.dto.role_permission.request.RolePermissionRequestDTO;
import a_and_s_service.compile.module.dto.role_permission.request.RolePermissionUpdateRequestDTO;
import a_and_s_service.compile.module.dto.role_permission.response.RolePermissionResponseDTO;
import a_and_s_service.compile.module.entity.RolePermissionEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RolePermissionService {
//    RolePermissionResponseDTO insertRolePermission(RolePermissionRequestDTO rolePermissionRequestDTO);
//
//    RolePermissionResponseDTO updateRolePermission(RolePermissionUpdateRequestDTO rolePermissionUpdateRequestDTO);
//
    void deleteByRoleId(UUID roleId);

    void saveAllRolePermission(Set<RolePermissionEntity> rolePermissionEntitySet);

}
