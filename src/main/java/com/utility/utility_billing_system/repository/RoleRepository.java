package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.Role;
import com.utility.utility_billing_system.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.Role} entities.
 * <p>
 * Used during startup seeding and role assignment to resolve RoleType to entity.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);
}
