package fsoft.franchise.auth.module.service.email;

public interface MailSenderService {
    public void sendOtpToEmail(String email, String otp);
}
