package fsoft.franchise.auth.module.dto.role.response;

import fsoft.franchise.auth.module.dto.permission.response.PermissionResponseDTO;

import java.util.List;
import java.util.UUID;

public record RoleResponseDTO(
        UUID id,
        String code,
        String name,
        String description,
        List<PermissionResponseDTO> permissions
) {}
