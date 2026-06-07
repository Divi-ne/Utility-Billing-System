-- Admin-created accounts must set their own password after email OTP verification

ALTER TABLE users
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users SET must_change_password = FALSE;
