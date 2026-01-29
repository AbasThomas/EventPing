-- V6__Advanced_Monetization_Schema.sql

-- 1. Modify plans table for unlimited usage, enterprise handling and bundled credits
ALTER TABLE plans ALTER COLUMN max_events_per_day DROP NOT NULL;
ALTER TABLE plans ALTER COLUMN max_participants_per_event DROP NOT NULL;
ALTER TABLE plans ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE plans ADD COLUMN IF NOT EXISTS is_enterprise BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE plans ADD COLUMN IF NOT EXISTS monthly_credit_limit INTEGER; -- NULL means unlimited

-- 2. Create plan_prices table for regional/currency support
DROP TABLE IF EXISTS plan_prices;
CREATE TABLE plan_prices (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount DECIMAL(19,2),
    region VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    billing_period VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    CONSTRAINT fk_price_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE,
    CONSTRAINT unique_plan_price UNIQUE (plan_id, currency, region, billing_period)
);

-- 3. Re-seed plans with the new "Growth-friendly" pricing, unlimited limits and bundled credits
UPDATE plans SET is_active = false; -- Deactivate old definitions

-- Update FREE ($0) - 100 credits/mo
UPDATE plans SET 
    max_events_per_day = 3, 
    max_participants_per_event = 20, 
    monthly_credit_limit = 100,
    is_enterprise = false,
    is_active = true,
    reminder_channels = 'EMAIL'
WHERE name = 'FREE';

-- Update BASIC ($5/mo) - 1000 credits/mo
UPDATE plans SET 
    max_events_per_day = 10, 
    max_participants_per_event = 100, 
    monthly_credit_limit = 1000,
    is_enterprise = false,
    is_active = true,
    reminder_channels = 'EMAIL,TELEGRAM'
WHERE name = 'BASIC';

-- Update PRO ($15/mo) - 5000 credits/mo
UPDATE plans SET 
    max_events_per_day = 50, 
    max_participants_per_event = 500, 
    monthly_credit_limit = 5000,
    is_enterprise = false,
    is_active = true,
    reminder_channels = 'EMAIL,TELEGRAM,WHATSAPP'
WHERE name = 'PRO';

-- Update BUSINESS ($45/mo) - 25000 credits/mo
UPDATE plans SET 
    max_events_per_day = NULL, -- Unlimited events
    max_participants_per_event = 2000, 
    monthly_credit_limit = 25000,
    is_enterprise = false,
    is_active = true,
    reminder_channels = 'EMAIL,TELEGRAM,WHATSAPP,DISCORD'
WHERE name = 'BUSINESS';

-- Update ENTERPRISE (Custom) - Unlimited credits
UPDATE plans SET 
    max_events_per_day = NULL, 
    max_participants_per_event = NULL, 
    monthly_credit_limit = NULL, -- Unlimited
    is_enterprise = true,
    is_active = true,
    reminder_channels = 'EMAIL,TELEGRAM,WHATSAPP,DISCORD'
WHERE name = 'ENTERPRISE';

-- 4. Seed default USD prices in plan_prices
INSERT INTO plan_prices (plan_id, currency, amount, region, billing_period)
VALUES 
((SELECT id FROM plans WHERE name = 'FREE'), 'USD', 0.00, 'GLOBAL', 'MONTHLY'),
((SELECT id FROM plans WHERE name = 'BASIC'), 'USD', 5.00, 'GLOBAL', 'MONTHLY'),
((SELECT id FROM plans WHERE name = 'PRO'), 'USD', 15.00, 'GLOBAL', 'MONTHLY'),
((SELECT id FROM plans WHERE name = 'BUSINESS'), 'USD', 45.00, 'GLOBAL', 'MONTHLY'),
((SELECT id FROM plans WHERE name = 'ENTERPRISE'), 'USD', NULL, 'GLOBAL', 'MONTHLY')
ON CONFLICT (plan_id, currency, region, billing_period) DO NOTHING;

-- 5. Cleanup the old price column from plans table once data is moved
-- ALTER TABLE plans DROP COLUMN price;
