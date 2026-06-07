package com.utility.utility_billing_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Outbound email sender settings for OTP messages.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private String from = "noreply@utility.com";
    private String fromName = "Utility Billing System";
}
