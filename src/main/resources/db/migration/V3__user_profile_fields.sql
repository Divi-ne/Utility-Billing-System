-- =============================================================================
-- V3: User Profile Fields
-- Adds full_name and phone_number; removes username (email is now the login ID)
-- =============================================================================

-- Add new profile columns (nullable first so existing rows can be migrated)
ALTER TABLE users ADD COLUMN full_name VARCHAR(150);
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);

-- Back-fill existing users before enforcing NOT NULL
UPDATE users
SET full_name = COALESCE(full_name, split_part(email, '@', 1)),
    phone_number = COALESCE(phone_number, '0000000000')
WHERE full_name IS NULL OR phone_number IS NULL;

-- Enforce required fields going forward
ALTER TABLE users ALTER COLUMN full_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN phone_number SET NOT NULL;

-- Remove legacy username column; authentication uses email only
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_username_key;
ALTER TABLE users DROP COLUMN username;
