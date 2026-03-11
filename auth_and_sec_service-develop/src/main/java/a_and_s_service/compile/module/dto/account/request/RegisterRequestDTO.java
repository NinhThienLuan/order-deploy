package a_and_s_service.compile.module.dto.account.request;

import a_and_s_service.compile.module.enumType.GenderEnum;

import java.time.LocalDate;

public record RegisterRequestDTO(
        String email,
        String phoneNumber,
        String password,        // null = không đổi password
        String firstName,
        String lastName,
        GenderEnum gender,
        LocalDate birthDate
) {}

