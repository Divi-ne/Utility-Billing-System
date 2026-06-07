package com.utility.utility_billing_system.entity;

import com.utility.utility_billing_system.enums.StatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a system user account.
 * <p>
 * Users authenticate with email/password and hold one or more roles.
 * Customers have an optional one-to-one link to a {@link Customer} profile.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    /** Unique login identifier and JWT subject claim. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /** BCrypt-hashed password; never exposed in API responses. */
    @Column(nullable = false)
    private String password;

    /** True after the user verifies their email via OTP. */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    /** When true, login is blocked until the user sets a password via {@code POST /api/auth/password/set}. */
    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private boolean mustChangePassword = false;

    /** ACTIVE users can log in; INACTIVE accounts are rejected. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusType status = StatusType.ACTIVE;

    /** Assigned roles determining endpoint access (ADMIN, OPERATOR, etc.). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /** Linked customer profile when user has the CUSTOMER role. */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Customer customer;
}
