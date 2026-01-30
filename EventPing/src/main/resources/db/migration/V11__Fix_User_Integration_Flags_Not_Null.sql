-- V11__Fix_User_Integration_Flags_Not_Null.sql
-- Ensure User integration flags are NOT NULL and have a default value

-- Update existing NULL values to false
UPDATE users SET enable_whatsapp = false WHERE enable_whatsapp IS NULL;
UPDATE users SET enable_discord = false WHERE enable_discord IS NULL;
UPDATE users SET enable_gmail = false WHERE enable_gmail IS NULL;
UPDATE users SET enable_google_calendar = false WHERE enable_google_calendar IS NULL;
UPDATE users SET enable_slack = false WHERE enable_slack IS NULL;

-- Alter columns to NOT NULL and add DEFAULT false
ALTER TABLE users 
    ALTER COLUMN enable_whatsapp SET NOT NULL,
    ALTER COLUMN enable_whatsapp SET DEFAULT false,
    ALTER COLUMN enable_discord SET NOT NULL,
    ALTER COLUMN enable_discord SET DEFAULT false,
    ALTER COLUMN enable_gmail SET NOT NULL,
    ALTER COLUMN enable_gmail SET DEFAULT false,
    ALTER COLUMN enable_google_calendar SET NOT NULL,
    ALTER COLUMN enable_google_calendar SET DEFAULT false,
    ALTER COLUMN enable_slack SET NOT NULL,
    ALTER COLUMN enable_slack SET DEFAULT false;
