package fsoft.franchise.auth.module.dto.login.response;

public record LoginResponseDTO (
    String accessToken,
    String refreshToken,
    Long expiresIn
) {
}
