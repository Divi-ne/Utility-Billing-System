package com.utility.utility_billing_system.dto.user;

import com.utility.utility_billing_system.enums.RoleType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Request body for admin role assignment ({@code PUT /api/users/{id}/roles}).
 * <p>
 * Replaces the user's entire role set with the provided roles.
 */
@Getter
@Setter
public class UserRoleUpdateRequest {

    @NotEmpty
    private Set<RoleType> roles;
}
