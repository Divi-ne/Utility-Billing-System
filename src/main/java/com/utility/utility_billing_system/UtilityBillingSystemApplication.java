package com.utility.utility_billing_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Utility Billing System Spring Boot application.
 * <p>
 * Bootstraps the application context, auto-configures Spring components (web layer,
 * security, JPA, Flyway migrations), and starts the embedded web server. All REST
 * endpoints under {@code /api/**} become available once this class is launched.
 */
@SpringBootApplication
public class UtilityBillingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtilityBillingSystemApplication.class, args);
	}

}
