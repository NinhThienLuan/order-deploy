package fsoft.franchise.auth.module.dto.account.response;

import fsoft.franchise.auth.module.enumType.GenderEnum;
import fsoft.franchise.auth.module.enumType.StatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponseForUserDTO(
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
        LocalDate birthDate
) {
}
