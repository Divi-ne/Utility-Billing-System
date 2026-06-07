package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.config.OtpProperties;
import com.utility.utility_billing_system.entity.EmailOtp;
import com.utility.utility_billing_system.entity.User;
import com.utility.utility_billing_system.enums.OtpPurpose;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.repository.EmailOtpRepository;
import com.utility.utility_billing_system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Generates, emails, and validates one-time passwords for account email confirmation.
 */
@Slf4j
@Service
public class OtpService {

    private final EmailOtpRepository emailOtpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final OtpProperties otpProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            EmailOtpRepository emailOtpRepository,
            UserRepository userRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            OtpProperties otpProperties) {
        this.emailOtpRepository = emailOtpRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.otpProperties = otpProperties;
    }

    /**
     * Sends a confirmation OTP to an existing unverified account.
     *
     * @return the generated OTP when delivery mode is CONSOLE; otherwise {@code null}
     */
    @Transactional
    public String sendConfirmationOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        validateUnverifiedUserExists(normalizedEmail);

        emailOtpRepository.invalidateActiveOtps(normalizedEmail, OtpPurpose.VERIFY_EMAIL);

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpProperties.getExpirationMinutes());

        EmailOtp record = EmailOtp.builder()
                .email(normalizedEmail)
                .otpHash(passwordEncoder.encode(otp))
                .purpose(OtpPurpose.VERIFY_EMAIL)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        emailOtpRepository.save(record);

        emailService.sendOtpEmail(normalizedEmail, otp, otpProperties.getExpirationMinutes());
        log.info("Confirmation OTP sent to {}", normalizedEmail);
        return otpProperties.isConsoleDelivery() ? otp : null;
    }

    /**
     * Validates the OTP and marks the account as email-verified.
     */
    @Transactional
    public void confirmAccount(String email, String otp) {
        findAndConsumeOtp(email, otp);

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Account confirmed for: {}", user.getEmail());
    }

    private void findAndConsumeOtp(String email, String otp) {
        String normalizedEmail = email.trim().toLowerCase();
        EmailOtp activeOtp = emailOtpRepository
                .findTopByEmailAndPurposeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        normalizedEmail, OtpPurpose.VERIFY_EMAIL, LocalDateTime.now())
                .orElseThrow(() -> new BusinessRuleException("Invalid or expired OTP"));

        if (!passwordEncoder.matches(otp, activeOtp.getOtpHash())) {
            throw new BusinessRuleException("Invalid or expired OTP");
        }

        activeOtp.setUsed(true);
        emailOtpRepository.save(activeOtp);
    }

    private void validateUnverifiedUserExists(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BusinessRuleException("Email is already verified");
        }
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, otpProperties.getLength());
        int floor = bound / 10;
        int code = secureRandom.nextInt(bound - floor) + floor;
        return String.valueOf(code);
    }
}
