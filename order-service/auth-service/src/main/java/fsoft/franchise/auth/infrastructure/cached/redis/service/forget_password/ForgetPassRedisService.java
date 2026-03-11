package fsoft.franchise.auth.infrastructure.cached.redis.service.forget_password;

import fsoft.franchise.auth.infrastructure.cached.redis.helpers.RedisSupported;
import fsoft.franchise.auth.infrastructure.cached.redis.keys.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForgetPassRedisService {

    private final RedisSupported redisSupported;

//    OTP for forget password: key = OTP_FORGET_PASSWORD:{email}, value = otp, ttl = 5 minutes
    public void saveForgetPassOTP(String email, String otp){
        String key = String.format(RedisKeys.OTP_FORGET_PASSWORD, email);
        redisSupported.setKeyWithTTL(key, otp, 5);
    }

    public String getForgetPassOTP(String email){
        String key = String.format(RedisKeys.OTP_FORGET_PASSWORD, email);
        return redisSupported.getKey(key);
    }

    public void deleteForgetPassOTP(String email){
        String key = String.format(RedisKeys.OTP_FORGET_PASSWORD, email);
        redisSupported.deleteKey(key);
    }

//    Token will generate after user verify OTP successfully,
//    key = VERIFY_TOKEN_FORGET_PASSWORD:{email}, value = token, ttl = 5 minutes

    public void saveForgetPassToken(String email, String verifyToken) {
        String key = String.format(RedisKeys.VERIFY_TOKEN_FORGET_PASSWORD, email);
        redisSupported.setKeyWithTTL(key, verifyToken, 5);
    }

    public String getForgetPassToken(String email) {
        String key = String.format(RedisKeys.VERIFY_TOKEN_FORGET_PASSWORD, email);
        return redisSupported.getKey(key);
    }

    public void deleteForgetPassToken(String email) {
        String key = String.format(RedisKeys.VERIFY_TOKEN_FORGET_PASSWORD, email);
        redisSupported.deleteKey(key);
    }




}
