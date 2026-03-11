package a_and_s_service.compile.module.dto.role_permission.response;

import java.util.UUID;

public record RolePermissionResponseDTO(
        UUID roleId,
        String roleCode,
        UUID permissionId,
        String permissionModule,
        String permissionCode
) {
}
