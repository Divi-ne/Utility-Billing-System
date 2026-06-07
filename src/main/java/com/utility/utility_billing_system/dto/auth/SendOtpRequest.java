package com.utility.utility_billing_system.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to resend an account confirmation code after self-signup.
 */
@Getter
@Setter
public class SendOtpRequest {

    @NotBlank
    @Email
    private String email;
}
