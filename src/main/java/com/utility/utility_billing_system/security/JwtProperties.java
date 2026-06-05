package com.utility.utility_billing_system.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
/**
 * Configuration properties for JWT signing and token lifetime.
 * <p>
 * Bound to {@code app.jwt.secret} and {@code app.jwt.expiration-ms} in application.properties.
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs;
}
