package fsoft.franchise.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long expiredIn) {
}
