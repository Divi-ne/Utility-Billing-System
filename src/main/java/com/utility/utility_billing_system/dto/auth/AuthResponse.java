package com.utility.utility_billing_system.dto.auth;

import com.utility.utility_billing_system.enums.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Response returned after successful signup or login.
 * <p>
 * Contains the JWT bearer token, token type, and basic user identity/role information.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private StatusType status;
    private boolean emailVerified;
    private Set<String> roles;
}
