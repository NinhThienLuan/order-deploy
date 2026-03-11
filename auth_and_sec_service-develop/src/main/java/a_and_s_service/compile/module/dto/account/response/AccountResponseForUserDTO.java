package a_and_s_service.compile.module.dto.account.response;

import a_and_s_service.compile.module.enumType.GenderEnum;
import a_and_s_service.compile.module.enumType.StatusEnum;

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
