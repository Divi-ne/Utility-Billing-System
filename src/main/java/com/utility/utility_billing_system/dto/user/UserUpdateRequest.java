package com.utility.utility_billing_system.dto.user;

import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for admin user updates ({@code PUT /api/users/{id}}).
 */
@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.PHONE_NUMBER, message = ValidationPatterns.PHONE_NUMBER_MESSAGE)
    private String phoneNumber;

    @NotNull
    private StatusType status;
}
