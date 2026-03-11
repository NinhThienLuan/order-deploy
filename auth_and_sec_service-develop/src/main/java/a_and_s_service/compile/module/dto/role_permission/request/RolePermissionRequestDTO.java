package a_and_s_service.compile.module.dto.role_permission.request;

import java.util.UUID;

public record RolePermissionRequestDTO(
        UUID roleId,
        UUID permissionId
) {
}
