package com.utility.utility_billing_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
/**
 * Configuration properties for the default admin user seed.
 * <p>
 * Bound to {@code app.seed.admin.*} properties. Controls whether a default
 * admin account is created on startup and its initial credentials.
 */
@Component
@ConfigurationProperties(prefix = "app.seed.admin")
public class AdminSeedProperties {

    private boolean enabled = true;
    private String fullName = "System Administrator";
    private String email = "admin@utility.com";
    private String phoneNumber = "0788000001";
    private String password = "Admin@123";
}
