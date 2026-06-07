package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.config.OtpProperties;
import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.dto.user.UserRequest;
import com.utility.utility_billing_system.dto.user.UserResponse;
import com.utility.utility_billing_system.dto.user.UserRoleUpdateRequest;
import com.utility.utility_billing_system.dto.user.UserUpdateRequest;
import com.utility.utility_billing_system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for system user account management.
 * <p>
 * Customers can view their own account details. Admins create and manage all users,
 * including role assignment and account deletion.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;
    private final OtpProperties otpProperties;

    public UserController(UserService userService, OtpProperties otpProperties) {
        this.userService = userService;
        this.otpProperties = otpProperties;
    }

    /** Returns the authenticated customer's user account. Role: CUSTOMER. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get own user account (customer)")
    public ResponseEntity<ApiResponse<UserResponse>> getMyAccount() {
        return ResponseEntity.ok(wrap(userService.getMyAccount(), "Account retrieved"));
    }

    /** Creates a new user with roles and credentials. Role: ADMIN. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        String message = otpProperties.isEnabled()
                ? "User created. A confirmation code was sent to their email address."
                : "User created";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wrap(response, message));
    }

    /** Lists all registered users. Role: ADMIN. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (admin)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(wrap(userService.getAllUsers(), "Users retrieved"));
    }

    /** Retrieves a user by ID. Role: ADMIN. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(userService.getUserById(id), "User retrieved"));
    }

    /** Assigns or revokes roles for a user. Role: ADMIN. */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upgrade or revoke user roles (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> updateRoles(
            @PathVariable Long id, @Valid @RequestBody UserRoleUpdateRequest request) {
        return ResponseEntity.ok(wrap(userService.updateRoles(id, request), "User roles updated"));
    }

    /** Updates user profile fields. Role: ADMIN. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user profile (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(wrap(userService.updateUser(id, request), "User updated"));
    }

    /** Deletes a user account. Role: ADMIN. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(wrap(null, "User deleted"));
    }

    private <T> ApiResponse<T> wrap(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}
