package fsoft.franchise.auth.module.dto.role.request;

import java.util.List;
import java.util.UUID;

public record RoleRequestDTO(
        String code,
        String name,
        String description,

        List<UUID> permissionIds) {
}
