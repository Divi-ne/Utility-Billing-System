package com.utility.utility_billing_system.dto.customer;

import com.utility.utility_billing_system.enums.StatusType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for staff creating or updating a customer profile.
 * <p>
 * Includes optional userId to link an existing user account.
 */
@Getter
@Setter
public class CustomerRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Size(max = 50)
    private String nationalId;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phoneNumber;

    @NotBlank
    @Size(max = 255)
    private String address;

    private StatusType status;

    private Long userId;
}
