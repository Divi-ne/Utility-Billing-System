package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.EmailOtp;
import com.utility.utility_billing_system.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Persistence for email OTP codes used during signup and email verification.
 */
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByEmailAndPurposeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, OtpPurpose purpose, LocalDateTime now);

    @Modifying
    @Query("UPDATE EmailOtp o SET o.used = true WHERE o.email = :email AND o.purpose = :purpose AND o.used = false")
    void invalidateActiveOtps(@Param("email") String email, @Param("purpose") OtpPurpose purpose);
}
