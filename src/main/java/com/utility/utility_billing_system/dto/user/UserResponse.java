package com.utility.utility_billing_system.dto.user;

import com.utility.utility_billing_system.enums.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO representing a system user account (password excluded).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private StatusType status;
    private boolean emailVerified;
    private boolean mustChangePassword;
    private Set<String> roles;
    private LocalDateTime createdAt;
}
