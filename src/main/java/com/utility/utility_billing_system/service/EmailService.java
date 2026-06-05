package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.config.MailProperties;
import com.utility.utility_billing_system.config.OtpProperties;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails such as OTP verification codes.
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final OtpProperties otpProperties;
    private final String mailUsername;

    public EmailService(
            JavaMailSender mailSender,
            MailProperties mailProperties,
            OtpProperties otpProperties,
            @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.otpProperties = otpProperties;
        this.mailUsername = mailUsername;
    }

    /**
     * Sends a one-time password to the given email address.
     */
    public void sendOtpEmail(String toEmail, String otp, int expirationMinutes) {
        if (otpProperties.isLogToConsole()) {
            log.info("OTP for {}: {} (valid {} minutes)", toEmail, otp, expirationMinutes);
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(resolveFromAddress());
            message.setTo(toEmail);
            message.setSubject("Your account confirmation code - Utility Billing");
            message.setText(buildOtpBody(otp, expirationMinutes));
            mailSender.send(message);
            log.info("Confirmation email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send confirmation email to {}: {}", toEmail, ex.getMessage(), ex);
            if (otpProperties.isLogToConsole()) {
                log.warn("Email delivery failed — use the OTP printed above from the server log");
                return;
            }
            throw new BusinessRuleException("Unable to send confirmation email. Please try again later.");
        }
    }

    private String resolveFromAddress() {
        if (mailUsername != null && !mailUsername.isBlank()) {
            return mailUsername.trim();
        }
        return mailProperties.getFrom();
    }

    private String buildOtpBody(String otp, int expirationMinutes) {
        return """
                Hello,

                Your account confirmation code is: %s

                This code expires in %d minutes. Do not share it with anyone.

                If you did not request this code, you can ignore this email.

                — %s
                """.formatted(otp, expirationMinutes, mailProperties.getFromName());
    }
}
