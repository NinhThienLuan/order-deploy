package fsoft.franchise.dto.auth;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String module,
        String code
) {}
