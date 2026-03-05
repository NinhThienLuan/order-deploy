package fsoft.franchise.dto.auth;

import java.util.UUID;

public record RoleResponse(
                UUID id,
                String code,
                String name,
                String description
// List<PermissionResponse> permissions
) {
}
