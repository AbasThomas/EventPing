-- V7__Add_Bundled_Credits_And_Cleanup.sql

-- 1. Add monthly_credit_limit to plans
ALTER TABLE plans ADD COLUMN IF NOT EXISTS monthly_credit_limit INTEGER; -- NULL means unlimited


-- 2. Update plan definitions with bundled credits and tiered value
-- FREE: 100 credits/mo
UPDATE plans SET 
    monthly_credit_limit = 100,
    max_events_per_day = 3,
    max_participants_per_event = 20,
    reminder_channels = 'EMAIL'
WHERE name = 'FREE';

-- BASIC: 1000 credits/mo ($5/mo)
UPDATE plans SET 
    monthly_credit_limit = 1000,
    max_events_per_day = 10,
    max_participants_per_event = 100,
    reminder_channels = 'EMAIL,TELEGRAM'
WHERE name = 'BASIC';

-- PRO: 5000 credits/mo ($15/mo)
UPDATE plans SET 
    monthly_credit_limit = 5000,
    max_events_per_day = 50,
    max_participants_per_event = 500,
    reminder_channels = 'EMAIL,TELEGRAM,WHATSAPP'
WHERE name = 'PRO';

-- BUSINESS: 25000 credits/mo ($45/mo)
UPDATE plans SET 
    monthly_credit_limit = 25000,
    max_events_per_day = NULL, -- Unlimited events
    max_participants_per_event = 2000,
    reminder_channels = 'EMAIL,TELEGRAM,WHATSAPP,DISCORD'
WHERE name = 'BUSINESS';

-- ENTERPRISE: Unlimited credits (Contact Sales)
UPDATE plans SET 
    monthly_credit_limit = NULL, -- Unlimited
    max_events_per_day = NULL,
    max_participants_per_event = NULL,
    reminder_channels = 'EMAIL,TELEGRAM,WHATSAPP,DISCORD',
    is_enterprise = true
WHERE name = 'ENTERPRISE';

-- 3. Remove add_ons table as per instructions (Do not support separate credit purchases)
DROP TABLE IF EXISTS add_ons;

-- 4. Delete existing add_on entity if logic was started (Optional)

-- This is handled by Java code removal later if needed
