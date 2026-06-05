package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.dto.auth.AuthResponse;
import com.utility.utility_billing_system.dto.auth.LoginRequest;
import com.utility.utility_billing_system.dto.auth.OtpResponse;
import com.utility.utility_billing_system.dto.auth.SendOtpRequest;
import com.utility.utility_billing_system.dto.auth.SignupRequest;
import com.utility.utility_billing_system.dto.auth.SignupResponse;
import com.utility.utility_billing_system.dto.auth.VerifyOtpRequest;
import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user authentication and account confirmation.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Signup, login, and account confirmation")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Resends a confirmation OTP for a self-registered unverified account. */
    @PostMapping("/otp/send")
    @Operation(summary = "Resend account confirmation code")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.<OtpResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build());
    }

    /** Confirms a self-registered account with the OTP from email. */
    @PostMapping("/otp/verify")
    @Operation(summary = "Confirm account with OTP")
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.<OtpResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build());
    }

    /** Registers a new customer; a confirmation OTP is emailed automatically. */
    @PostMapping("/signup")
    @Operation(summary = "Customer self-registration")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SignupResponse>builder()
                        .success(true)
                        .message("Registration successful. You can log in now.")
                        .data(response)
                        .build());
    }

    /** Authenticates a user and returns a JWT. */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .build());
    }
}
