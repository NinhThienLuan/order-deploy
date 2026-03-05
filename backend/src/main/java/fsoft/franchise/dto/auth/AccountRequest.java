package fsoft.franchise.dto.auth;

import fsoft.franchise.enums.GenderEnum;

import java.time.LocalDate;

public record AccountRequest(
        String email,
        String phoneNumber,
        String password,        // null = không đổi password
        String firstName,
        String lastName,
        GenderEnum gender,
        LocalDate birthDate
) {}