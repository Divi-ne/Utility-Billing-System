package com.utility.utility_billing_system.config;

import com.utility.utility_billing_system.entity.Role;
import com.utility.utility_billing_system.entity.User;
import com.utility.utility_billing_system.enums.RoleType;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.repository.RoleRepository;
import com.utility.utility_billing_system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Startup seeder that creates a default admin user if one does not exist.
 * <p>
 * Runs after {@link DataInitializer} (Order 2) so roles are available.
 * Skipped when {@code app.seed.admin.enabled=false}.
 */
@Slf4j
@Component
@Order(2)
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminSeedProperties adminSeedProperties;

    public AdminSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AdminSeedProperties adminSeedProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminSeedProperties = adminSeedProperties;
    }

    @Override
    public void run(String... args) {
        if (!adminSeedProperties.isEnabled()) {
            return;
        }

        if (userRepository.existsByEmail(adminSeedProperties.getEmail())) {
            log.info("Seeded admin already exists: {}", adminSeedProperties.getEmail());
            return;
        }

        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found — run role initialization first"));

        User admin = User.builder()
                .fullName(adminSeedProperties.getFullName())
                .email(adminSeedProperties.getEmail())
                .phoneNumber(adminSeedProperties.getPhoneNumber())
                .password(passwordEncoder.encode(adminSeedProperties.getPassword()))
                .emailVerified(true)
                .mustChangePassword(false)
                .status(StatusType.ACTIVE)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Seeded default admin user: {}", adminSeedProperties.getEmail());
    }
}
