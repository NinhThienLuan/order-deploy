package a_and_s_service.compile.infrastructure.cached.redis.service.register;

import a_and_s_service.compile.infrastructure.cached.redis.helpers.RedisSupported;
import a_and_s_service.compile.infrastructure.cached.redis.keys.RedisKeys;
import a_and_s_service.compile.module.dto.account.request.RegisterRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterRedisService {

    private final RedisSupported redisSupported;
    private final ObjectMapper objectMapper;

    // 1. Lưu Data form đăng ký (Sống 30 phút để user thoải mái thao tác)
    public void saveRegisterData(String email, RegisterRequestDTO requestDTO) {
        String dataJson = objectMapper.writeValueAsString(requestDTO);
        String dataKey = String.format(RedisKeys.TEMP_DATA_REGISTER, email);
        redisSupported.setKeyWithTTL(dataKey, dataJson, 30);
    }

    // Lấy Data ra và map ngược lại thành DTO
    public RegisterRequestDTO getRegisterData(String email) {
        String dataJson = redisSupported.getKey(String.format(RedisKeys.TEMP_DATA_REGISTER, email));
        if (dataJson == null) return null; // Hết 30 phút là bay màu

        return objectMapper.readValue(dataJson, RegisterRequestDTO.class);
    }

    // 2. Lưu mã OTP (Chỉ sống 5 phút cho an toàn)
    public void saveRegisterOtp(String email, String otp) {
        String otpKey = String.format(RedisKeys.OTP_REGISTER, email);
        redisSupported.setKeyWithTTL(otpKey, otp, 5);
    }

    public String getRegisterOtp(String email) {
        return redisSupported.getKey(String.format(RedisKeys.OTP_REGISTER, email));
    }

    // 3. Quét dọn sạch sẽ sau khi đăng ký thành công
    public void clearRegisterSession(String email) {
        redisSupported.deleteKey(String.format(RedisKeys.OTP_REGISTER, email));
        redisSupported.deleteKey(String.format(RedisKeys.TEMP_DATA_REGISTER, email));
    }
}
