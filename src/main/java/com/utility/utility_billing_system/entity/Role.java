package com.utility.utility_billing_system.entity;

import com.utility.utility_billing_system.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a security role.
 * <p>
 * Roles are seeded at startup and referenced by users via a many-to-many relationship.
 * Role names map to Spring Security authorities and {@code @PreAuthorize} expressions.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    /** Role identifier (e.g. ROLE_ADMIN, ROLE_CUSTOMER). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private RoleType name;
}
