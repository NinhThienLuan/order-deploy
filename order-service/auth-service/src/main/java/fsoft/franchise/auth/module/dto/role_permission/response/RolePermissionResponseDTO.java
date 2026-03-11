package fsoft.franchise.auth.module.dto.role_permission.response;

import java.util.UUID;

public record RolePermissionResponseDTO(
        UUID roleId,
        String roleCode,
        UUID permissionId,
        String permissionModule,
        String permissionCode
) {
}
