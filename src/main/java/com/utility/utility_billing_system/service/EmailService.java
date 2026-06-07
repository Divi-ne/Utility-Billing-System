package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.config.MailProperties;
import com.utility.utility_billing_system.config.OtpProperties;
import com.utility.utility_billing_system.entity.Customer;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails such as OTP verification codes and payment confirmations.
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
        if (otpProperties.isConsoleDelivery() || otpProperties.isLogToConsole()) {
            log.info("OTP for {}: {} (valid {} minutes)", toEmail, otp, expirationMinutes);
        }

        if (otpProperties.isConsoleDelivery()) {
            log.info("Console OTP delivery enabled — email was not sent");
            return;
        }

        try {
            sendEmail(toEmail, "Your account confirmation code - Utility Billing", buildOtpBody(otp, expirationMinutes));
            log.info("Confirmation email sent to {}", toEmail);
        } catch (BusinessRuleException ex) {
            if (otpProperties.isLogToConsole()) {
                log.warn("Email delivery failed — use the OTP printed above from the server log");
                return;
            }
            throw ex;
        }
    }

    /**
     * Sends a customer notification email (bill approval, payment confirmation, etc.).
     */
    public void sendCustomerNotificationEmail(Customer customer, String subject, String message) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            log.warn("Customer notification email skipped — customer email not available");
            return;
        }

        String toEmail = customer.getEmail().trim().toLowerCase();

        try {
            sendEmail(toEmail, subject, message);
        } catch (BusinessRuleException ex) {
            log.error("Notification email could not be sent to {}: {}", toEmail, ex.getMessage());
        }
    }

    /**
     * Sends a payment confirmation email using the same message as the in-app notification.
     */
    public void sendPaymentConfirmationEmail(Customer customer, String message) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            log.warn("Payment confirmation email skipped — customer email not available");
            return;
        }

        sendCustomerNotificationEmail(customer, "Payment confirmation - Utility Billing", message);
    }

    /** Sends a transactional email directly to the recipient's inbox (never console-only). */
    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(resolveFromAddress());
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", toEmail, ex.getMessage(), ex);
            throw new BusinessRuleException("Unable to send email to " + toEmail + ". Check mail configuration.");
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
