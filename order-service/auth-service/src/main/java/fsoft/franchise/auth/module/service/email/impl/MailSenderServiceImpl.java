package fsoft.franchise.auth.module.service.email.impl;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;
import fsoft.franchise.auth.module.service.email.MailSenderService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpToEmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("khanhviet183@gmail.com");
            helper.setTo(email);
            helper.setSubject("OTP Code For Forget Password");
            helper.setText(buildOtpEmailHtml(otp), true);

            mailSender.send(message);
            log.info("OTP email sent successfully to {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage());
            throw new ApiException(ErrorCode.EMAIL_SEND_FAILED, "Failed to send OTP email");
        }
    }

    private String buildOtpEmailHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>OTP Verification</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Arial', sans-serif; background-color: #f5f5f5;">
                    <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border: 2px solid #000000;">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 20px; text-align: center; background-color: #D98324;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: 0.05em;">
                                                FRANCHISE SERVICE
                                            </h1>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 20px; color: #000000; font-size: 24px; font-weight: 700;">
                                                Your Verification Code
                                            </h2>

                                            <p style="margin: 0 0 30px; color: #6B6B6B; font-size: 14px; line-height: 1.6;">
                                                Please use the following OTP code to complete your verification. This code will expire in <strong style="color: #D98324;">5 minutes</strong>.
                                            </p>

                                            <!-- OTP Box -->
                                            <table width="100%" cellpadding="0" cellspacing="0">
                                                <tr>
                                                    <td align="center" style="padding: 30px 0;">
                                                        <div style="display: inline-block; background-color: #000000; padding: 20px 40px; border: 2px solid #D98324;">
                                                            <span style="color: #D98324; font-size: 36px; font-weight: 700; letter-spacing: 0.3em;">
                                                                {{OTP_CODE}}
                                                            </span>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>

                                            <p style="margin: 30px 0 0; color: #6B6B6B; font-size: 13px; line-height: 1.6;">
                                                If you didn't request this code, please ignore this email or contact support if you have concerns.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #000000; text-align: center; border-top: 2px solid #D98324;">
                                            <p style="margin: 0; color: #9B9B9B; font-size: 12px;">
                                                &copy; 2026 Franchise Service. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .replace("{{OTP_CODE}}", otp);
    }
}
