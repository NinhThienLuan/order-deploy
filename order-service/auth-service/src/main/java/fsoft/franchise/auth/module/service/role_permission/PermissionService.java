package fsoft.franchise.auth.module.service.role_permission;

import fsoft.franchise.auth.module.dto.permission.request.PermissionRequestDTO;
import fsoft.franchise.auth.module.dto.permission.response.PermissionResponseDTO;
import fsoft.franchise.auth.module.entity.PermissionEntity;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
//    PermissionResponseDTO insertPermission(PermissionRequestDTO permissionRequestDTO);

//    PermissionResponseDTO updatePermission(UUID permissionId, PermissionRequestDTO permissionRequestDTO);

//    void deletePermission(UUID permissionId);

//    PermissionResponseDTO getPermissionById(UUID permissionId);

    PermissionEntity getPermissionByEntityId(UUID permissionId);

    List<PermissionResponseDTO> getAllPermission();

    List<PermissionEntity> getAllPermissionEntityByIds(List<UUID> permissionIds);
}
