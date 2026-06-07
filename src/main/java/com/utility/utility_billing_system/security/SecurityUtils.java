package com.utility.utility_billing_system.security;

import com.utility.utility_billing_system.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for accessing the current authenticated user from Spring Security context.
 * <p>
 * Used by services to resolve the logged-in user's email or full user details
 * without passing credentials through method parameters.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** Returns the email of the currently authenticated user. */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Not authenticated");
        }
        return authentication.getName();
    }

    /** Returns the full CustomUserDetails for the authenticated user. */
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return details;
    }
}
