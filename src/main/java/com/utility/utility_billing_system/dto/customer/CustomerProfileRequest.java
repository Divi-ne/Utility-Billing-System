package com.utility.utility_billing_system.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for a customer updating their own profile ({@code PUT /api/customers/me}).
 */
@Getter
@Setter
public class CustomerProfileRequest {

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
}
