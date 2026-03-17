package fsoft.franchise.auth.module.dto.permission.response;

import java.util.UUID;

public record PermissionResponseDTO(
        UUID id,
        String module,
        String code
) {}
