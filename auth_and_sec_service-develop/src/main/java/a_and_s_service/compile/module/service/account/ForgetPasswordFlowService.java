package a_and_s_service.compile.module.service.account;

public interface ForgetPasswordFlowService {
    //    Tạo hàm để generate OTP và gửi OTP cho email đồng thời lưu OTP vào Redis
    void processForgotPassword(String email);

    //    Tạo hàm để xác thực OTP đồng thời generate token khi xác thực OTP thành công
    String generateVerifyToken(String email, String otp);

    //    Tạo hàm để xác thực token và đổi mật khẩu mới
    void resetPassword(String email, String verifyToken, String newPassword);

    // Tạo hàm để resend OTP trong trường hợp user không nhận được OTP hoặc OTP hết hạn
    void resendOtp(String email);
}
