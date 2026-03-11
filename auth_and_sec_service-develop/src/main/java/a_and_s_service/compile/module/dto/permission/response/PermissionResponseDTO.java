package a_and_s_service.compile.module.dto.permission.response;

import java.util.UUID;

public record PermissionResponseDTO(
        UUID id,
        String module,
        String code
) {}
