package a_and_s_service.compile.module.service.role_permission;

import a_and_s_service.compile.module.dto.permission.request.PermissionRequestDTO;
import a_and_s_service.compile.module.dto.permission.response.PermissionResponseDTO;
import a_and_s_service.compile.module.entity.PermissionEntity;

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
