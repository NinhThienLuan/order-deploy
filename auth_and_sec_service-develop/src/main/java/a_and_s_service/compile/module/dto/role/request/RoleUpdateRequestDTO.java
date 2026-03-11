package a_and_s_service.compile.module.dto.role.request;

import java.util.List;
import java.util.UUID;

public record RoleUpdateRequestDTO(
        String name,
        String description,

        List<UUID> permissionIds
) {
}
