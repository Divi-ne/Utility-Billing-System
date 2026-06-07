package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.User} entities.
 * <p>
 * Provides email-based lookup used during login and authentication.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
