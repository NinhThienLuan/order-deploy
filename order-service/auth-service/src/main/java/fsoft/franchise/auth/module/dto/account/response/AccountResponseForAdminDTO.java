package fsoft.franchise.auth.module.dto.account.response;

import fsoft.franchise.auth.module.dto.role.response.RoleResponseDTO;
import fsoft.franchise.auth.module.enumType.GenderEnum;
import fsoft.franchise.auth.module.enumType.StatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AccountResponseForAdminDTO(
        // Account fields
        UUID id,
        String email,
        String phoneNumber,
        StatusEnum status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // Profile fields
        String firstName,
        String lastName,
        GenderEnum gender,
        LocalDate birthDate,

        // Roles kèm permissions
        List<RoleResponseDTO> roles
) {}
