package fsoft.franchise.auth.module.dto.account.request;

import fsoft.franchise.auth.module.enumType.GenderEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AdminCreateAccountRequestDTO(
        String email,
        String phoneNumber,
        String password,        // null = không đổi password
        String firstName,
        String lastName,
        GenderEnum gender,
        LocalDate birthDate,

        List<UUID> roleIds
) {
}
