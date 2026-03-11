package fsoft.franchise.auth.module.dto.role_permission.request;

import java.util.UUID;

public record RolePermissionUpdateRequestDTO(
            UUID roleId,
            UUID oldPermissionId,
            UUID newPermissionId) {
}
