package a_and_s_service.compile.module.dto.role.response;

import a_and_s_service.compile.module.dto.permission.response.PermissionResponseDTO;

import java.util.List;
import java.util.UUID;

public record RoleResponseDTO(
        UUID id,
        String code,
        String name,
        String description,
        List<PermissionResponseDTO> permissions
) {}
