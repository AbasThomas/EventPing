-- V8__Add_Usage_Tracking_To_Users.sql

ALTER TABLE users ADD COLUMN monthly_credits_used INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN last_usage_reset_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
