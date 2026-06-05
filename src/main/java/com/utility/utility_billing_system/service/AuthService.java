package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.config.OtpProperties;
import com.utility.utility_billing_system.dto.auth.AuthResponse;
import com.utility.utility_billing_system.dto.auth.LoginRequest;
import com.utility.utility_billing_system.dto.auth.OtpResponse;
import com.utility.utility_billing_system.dto.auth.SendOtpRequest;
import com.utility.utility_billing_system.dto.auth.SignupRequest;
import com.utility.utility_billing_system.dto.auth.SignupResponse;
import com.utility.utility_billing_system.dto.auth.VerifyOtpRequest;
import com.utility.utility_billing_system.entity.Role;
import com.utility.utility_billing_system.entity.User;
import com.utility.utility_billing_system.enums.RoleType;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import com.utility.utility_billing_system.exception.DuplicateResourceException;
import com.utility.utility_billing_system.exception.UnauthorizedException;
import com.utility.utility_billing_system.repository.RoleRepository;
import com.utility.utility_billing_system.repository.UserRepository;
import com.utility.utility_billing_system.security.CustomUserDetails;
import com.utility.utility_billing_system.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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
        otpService.sendConfirmationOtp(email);
        return OtpResponse.builder()
                .email(email)
                .expirationMinutes(otpProperties.getExpirationMinutes())
                .message("Confirmation code sent to your email")
                .build();
    }

    /**
     * Confirms a self-registered account using the emailed OTP.
     */
    public OtpResponse verifyOtp(VerifyOtpRequest request) {
        ensureOtpEnabled();
        String email = normalizeEmail(request.getEmail());
        otpService.confirmAccount(email, request.getOtp());
        return OtpResponse.builder()
                .email(email)
                .message("Account confirmed successfully. You can now log in.")
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
                .status(request.getStatus())
                .roles(Set.of(customerRole))
                .build();

        User saved = userRepository.save(user);
        customerService.createProfileForUser(saved, request.getNationalId(), request.getAddress());

        if (otpEnabled) {
            userRepository.flush();
            otpService.sendConfirmationOtp(saved.getEmail());
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
                throw new UnauthorizedException(
                        "Account not confirmed. Check your email for the confirmation code, or resend via POST /api/auth/otp/send");
            }

            log.info("User logged in: {}", userDetails.getUsername());
            return buildAuthResponse(userDetails.getUser());
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
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
