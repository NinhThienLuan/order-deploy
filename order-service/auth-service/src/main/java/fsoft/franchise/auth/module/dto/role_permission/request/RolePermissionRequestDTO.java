package fsoft.franchise.auth.module.dto.role_permission.request;

import java.util.UUID;

public record RolePermissionRequestDTO(
        UUID roleId,
        UUID permissionId
) {
}
