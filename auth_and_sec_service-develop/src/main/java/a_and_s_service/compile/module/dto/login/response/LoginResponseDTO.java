package a_and_s_service.compile.module.dto.login.response;

public record LoginResponseDTO (
    String accessToken,
    String refreshToken,
    Long expiresIn
) {
}
