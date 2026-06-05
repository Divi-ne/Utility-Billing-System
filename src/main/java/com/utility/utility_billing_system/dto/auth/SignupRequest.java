package com.utility.utility_billing_system.dto.auth;

import com.utility.utility_billing_system.enums.StatusType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
/**
 * Request body for customer self-registration ({@code POST /api/auth/signup}).
 * <p>
 * Collects user credentials and initial customer profile fields (national ID, address).
 */
@Getter
@Setter
public class SignupRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phoneNumber;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotNull
    private StatusType status;

    @NotBlank
    @Size(max = 50)
    private String nationalId;

    @NotBlank
    @Size(max = 255)
    private String address;
}
