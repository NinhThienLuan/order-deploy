package a_and_s_service.compile.infrastructure.cached.redis.keys;

public final class RedisKeys {

    private RedisKeys() {
    }

    private static final String PREFIX = "franchise";
    private static final String AUTH_PREFIX = ":auth:";

    //    OTP for register account of user and staff
    public static final String OTP_REGISTER = PREFIX + AUTH_PREFIX + ":otp:register:%s";
    public static final String TEMP_DATA_REGISTER = PREFIX + AUTH_PREFIX + ":otp:temp_data:%s";

    //    OTP and token for forget password of user and staff
    public static final String OTP_FORGET_PASSWORD = PREFIX + AUTH_PREFIX + ":otp:forget_password:%s";
    public static final String VERIFY_TOKEN_FORGET_PASSWORD = PREFIX + AUTH_PREFIX + ":verifyToken:forget_password:%s";
}
