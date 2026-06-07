package com.utility.utility_billing_system.dto.user;

import com.utility.utility_billing_system.enums.RoleType;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Request body for admin user creation ({@code POST /api/users}).
 */
@Getter
@Setter
public class UserRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.PHONE_NUMBER, message = ValidationPatterns.PHONE_NUMBER_MESSAGE)
    private String phoneNumber;

    /** Optional when OTP is enabled — the user sets their password after email verification. */
    @Size(min = 6, max = 100)
    private String password;

    @NotNull
    private StatusType status;

    @NotEmpty
    private Set<RoleType> roles;
}
