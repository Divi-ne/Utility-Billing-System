-- Email verification flag on users and OTP storage for email-based verification

ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Existing accounts (admin, etc.) are treated as already verified
UPDATE users SET email_verified = TRUE;

CREATE TABLE email_otps (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(100) NOT NULL,
    otp_hash    VARCHAR(255) NOT NULL,
    purpose     VARCHAR(30)  NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_otps_lookup ON email_otps (email, purpose, used, expires_at);
