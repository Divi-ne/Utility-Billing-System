package com.utility.utility_billing_system.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for user login ({@code POST /api/auth/login}).
 * <p>
 * Email and password are validated against the database; success returns a JWT.
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
