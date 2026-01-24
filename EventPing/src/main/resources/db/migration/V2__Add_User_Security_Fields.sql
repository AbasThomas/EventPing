-- Add security-related fields to users table
ALTER TABLE users 
ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER',
ADD COLUMN account_locked BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0,
ADD COLUMN last_login_at TIMESTAMP NULL;

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Create index on role for authorization queries
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Create index on account_locked for security queries
CREATE INDEX IF NOT EXISTS idx_users_account_locked ON users(account_locked);