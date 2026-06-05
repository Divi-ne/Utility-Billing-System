package com.utility.utility_billing_system.config;

import com.utility.utility_billing_system.entity.Role;
import com.utility.utility_billing_system.enums.RoleType;
import com.utility.utility_billing_system.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Startup initializer that seeds all {@link com.utility.utility_billing_system.enums.RoleType} values into the database.
 * <p>
 * Runs first on application startup (Order 1) to ensure roles exist before
 * admin seeding or user registration.
 */
@Slf4j
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        for (RoleType roleType : RoleType.values()) {
            roleRepository.findByName(roleType).orElseGet(() -> {
                Role role = Role.builder().name(roleType).build();
                Role saved = roleRepository.save(role);
                log.info("Initialized role: {}", roleType);
                return saved;
            });
        }
    }
}
