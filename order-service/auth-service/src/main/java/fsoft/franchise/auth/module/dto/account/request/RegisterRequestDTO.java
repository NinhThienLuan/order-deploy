package fsoft.franchise.auth.module.dto.account.request;

import fsoft.franchise.auth.module.enumType.GenderEnum;

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

