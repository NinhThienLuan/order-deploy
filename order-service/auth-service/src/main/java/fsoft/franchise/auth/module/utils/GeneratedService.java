package fsoft.franchise.auth.module.utils;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;

import java.security.SecureRandom;
import java.util.UUID;

public class GeneratedService {

    private static final SecureRandom random = new SecureRandom();

    private GeneratedService() {}

    public static String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
//            nextInt(10) sẽ trả về một số nguyên ngẫu nhiên từ 0 đến 9, do đó mỗi chữ số của OTP sẽ là một số từ 0 đến 9.
        }
        if (otp.isEmpty()){
            throw new ApiException(ErrorCode.UNEXPECTED_ERROR, "Failed to generate OTP");
        }
        return otp.toString();
    }

    public static String generateVerifyToken(){
        return UUID.randomUUID().toString();
    }
}
