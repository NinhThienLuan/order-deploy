package fsoft.franchise.dto.auth;

import fsoft.franchise.enums.GenderEnum;
import fsoft.franchise.enums.StatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AccountResponse(
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
        List<RoleResponse> roles
) {}