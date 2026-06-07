package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.config.OtpProperties;
import com.utility.utility_billing_system.dto.auth.AuthResponse;
import com.utility.utility_billing_system.dto.auth.LoginRequest;
import com.utility.utility_billing_system.dto.auth.OtpResponse;
import com.utility.utility_billing_system.dto.auth.SendOtpRequest;
import com.utility.utility_billing_system.dto.auth.SetPasswordRequest;
import com.utility.utility_billing_system.dto.auth.SignupRequest;
import com.utility.utility_billing_system.dto.auth.SignupResponse;
import com.utility.utility_billing_system.dto.auth.VerifyOtpRequest;
import com.utility.utility_billing_system.entity.Role;
import com.utility.utility_billing_system.entity.User;
import com.utility.utility_billing_system.enums.RoleType;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import com.utility.utility_billing_system.exception.DuplicateResourceException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.exception.UnauthorizedException;
import com.utility.utility_billing_system.repository.RoleRepository;
import com.utility.utility_billing_system.repository.UserRepository;
import com.utility.utility_billing_system.security.CustomUserDetails;
import com.utility.utility_billing_system.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service handling user registration, account confirmation, and authentication.
 */
@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    private final OtpService otpService;
    private final OtpProperties otpProperties;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            CustomerService customerService,
            OtpService otpService,
            OtpProperties otpProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.customerService = customerService;
        this.otpService = otpService;
        this.otpProperties = otpProperties;
    }

    /**
     * Resends a confirmation OTP for a self-registered unverified account.
     */
    public OtpResponse sendOtp(SendOtpRequest request) {
        ensureOtpEnabled();
        String email = normalizeEmail(request.getEmail());
        String otp = otpService.sendConfirmationOtp(email);
        return OtpResponse.builder()
                .email(email)
                .expirationMinutes(otpProperties.getExpirationMinutes())
                .otp(otp)
                .message(otpProperties.isConsoleDelivery()
                        ? "Confirmation code generated — use the otp field below to verify your account"
                        : "Confirmation code sent to your email")
                .build();
    }

    /**
     * Confirms a self-registered account using the emailed OTP.
     */
    public OtpResponse verifyOtp(VerifyOtpRequest request) {
        ensureOtpEnabled();
        String email = normalizeEmail(request.getEmail());
        otpService.confirmAccount(email, request.getOtp());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String message = user.isMustChangePassword()
                ? "Account confirmed. Set your password via POST /api/auth/password/set, then log in."
                : "Account confirmed successfully. You can now log in.";

        return OtpResponse.builder()
                .email(email)
                .message(message)
                .build();
    }

    /**
     * Sets the initial password for an admin-created account after email OTP verification.
     */
    @Transactional
    public OtpResponse setPassword(SetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new BusinessRuleException("Confirm your email with the OTP before setting a password");
        }
        if (!user.isMustChangePassword()) {
            throw new BusinessRuleException("Password is already set. Log in or contact an administrator.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        log.info("Initial password set for: {}", email);

        return OtpResponse.builder()
                .email(email)
                .message("Password set successfully. You can now log in.")
                .build();
    }

    /**
     * Registers a new customer. When OTP is enabled, sends a confirmation email before login.
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseThrow(() -> new DuplicateResourceException("Customer role not configured"));

        boolean otpEnabled = otpProperties.isEnabled();

        User user = User.builder()
                .fullName(request.getFullName())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(!otpEnabled)
                .mustChangePassword(false)
                .status(request.getStatus())
                .roles(Set.of(customerRole))
                .build();

        User saved = userRepository.save(user);
        customerService.createProfileForUser(saved, request.getNationalId(), request.getAddress());

        String otp = null;
        if (otpEnabled) {
            userRepository.flush();
            otp = otpService.sendConfirmationOtp(saved.getEmail());
            log.info("Customer registered — confirmation OTP sent to {}", saved.getEmail());
        } else {
            log.info("Customer registered (OTP disabled): {}", saved.getEmail());
        }

        return SignupResponse.builder()
                .userId(saved.getId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .status(saved.getStatus())
                .emailVerified(saved.isEmailVerified())
                .otp(otp)
                .expirationMinutes(otpEnabled ? otpProperties.getExpirationMinutes() : null)
                .build();
    }

    /**
     * Authenticates credentials and returns a JWT.
     */
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            if (otpProperties.isEnabled() && !userDetails.getUser().isEmailVerified()) {
                String hint = otpProperties.isConsoleDelivery()
                        ? "Resend via POST /api/auth/otp/send — the code is returned in the response"
                        : "Check your email for the confirmation code, or resend via POST /api/auth/otp/send";
                throw new UnauthorizedException("Account not confirmed. " + hint);
            }

            if (userDetails.getUser().isMustChangePassword()) {
                throw new UnauthorizedException(
                        "Set your password via POST /api/auth/password/set before logging in.");
            }

            log.info("User logged in: {}", userDetails.getUsername());
            return buildAuthResponse(userDetails.getUser());
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (DisabledException ex) {
            throw new UnauthorizedException("Account is inactive. Contact an administrator.");
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid email or password");
        } catch (Exception ex) {
            log.error("Login failed for {}: {}", email, ex.getMessage(), ex);
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    private void ensureOtpEnabled() {
        if (!otpProperties.isEnabled()) {
            throw new BusinessRuleException("Email confirmation is currently disabled");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
