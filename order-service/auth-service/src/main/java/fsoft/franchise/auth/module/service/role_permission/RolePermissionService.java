package fsoft.franchise.auth.module.service.role_permission;

import fsoft.franchise.auth.module.dto.role_permission.request.RolePermissionRequestDTO;
import fsoft.franchise.auth.module.dto.role_permission.request.RolePermissionUpdateRequestDTO;
import fsoft.franchise.auth.module.dto.role_permission.response.RolePermissionResponseDTO;
import fsoft.franchise.auth.module.entity.RolePermissionEntity;

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
