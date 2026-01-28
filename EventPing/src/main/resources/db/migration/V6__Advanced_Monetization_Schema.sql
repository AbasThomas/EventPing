-- V6__Advanced_Monetization_Schema.sql

-- 1. Modify plans table for unlimited usage and enterprise handling
ALTER TABLE plans ALTER COLUMN max_events_per_day DROP NOT NULL;
ALTER TABLE plans ALTER COLUMN max_participants_per_event DROP NOT NULL;
ALTER TABLE plans ALTER COLUMN price DROP NOT NULL;
ALTER TABLE plans ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE plans ADD COLUMN is_enterprise BOOLEAN NOT NULL DEFAULT false;

-- 2. Create plan_prices table for regional/currency support
CREATE TABLE plan_prices (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount DECIMAL(19,2),
    region VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    billing_period VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    CONSTRAINT fk_price_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_plan_prices_plan ON plan_prices(plan_id);
CREATE INDEX idx_plan_prices_region_currency ON plan_prices(region, currency);

-- 3. Create add_ons table
CREATE TABLE add_ons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN NOT NULL DEFAULT true,
    credit_amount INTEGER
);

-- 4. Re-seed plans with the new "Growth-friendly" pricing and unlimited limits
UPDATE plans SET is_active = false; -- Deactivate old definitions

-- Clear existing data if necessary (or just insert/update)
-- Since we are in a migration, it's safer to target by name

-- Update FREE
UPDATE plans SET 
    max_events_per_day = 3, 
    max_participants_per_event = 20, 
    price = 0, 
    is_enterprise = false,
    is_active = true
WHERE name = 'FREE';

-- Update BASIC ($5/mo)
UPDATE plans SET 
    max_events_per_day = 10, 
    max_participants_per_event = 100, 
    price = 5.00, 
    is_enterprise = false,
    is_active = true
WHERE name = 'BASIC';

-- Update PRO ($15/mo - shifted from $29.99 for growth)
UPDATE plans SET 
    max_events_per_day = 50, 
    max_participants_per_event = 500, 
    price = 15.00, 
    is_enterprise = false,
    is_active = true
WHERE name = 'PRO';

-- Update BUSINESS (Unlimited events, 2000 participants, $45/mo)
UPDATE plans SET 
    max_events_per_day = NULL, -- Unlimited
    max_participants_per_event = 2000, 
    price = 45.00, 
    is_enterprise = false,
    is_active = true
WHERE name = 'BUSINESS';

-- Update ENTERPRISE (Custom price)
UPDATE plans SET 
    max_events_per_day = NULL, -- Unlimited
    max_participants_per_event = NULL, -- Unlimited
    price = NULL, -- Custom/Contact Sales
    is_enterprise = true,
    is_active = true
WHERE name = 'ENTERPRISE';

-- 5. Seed default USD prices in plan_prices
INSERT INTO plan_prices (plan_id, currency, amount, region, billing_period)
SELECT id, 'USD', price, 'GLOBAL', 'MONTHLY' FROM plans WHERE is_active = true;

-- Enterprise is already NULL if we use the price column, but let's be explicit
UPDATE plan_prices SET amount = NULL WHERE plan_id = (SELECT id FROM plans WHERE name = 'ENTERPRISE');

-- 6. Add some standard add-ons
INSERT INTO add_ons (name, description, type, price, currency, credit_amount)
VALUES ('1k Email Pack', '1000 additional email credits', 'MESSAGING_CREDITS', 5.00, 'USD', 1000);

INSERT INTO add_ons (name, description, type, price, currency)
VALUES ('Single Event PRO Upgrade', 'Upgrade a single event to PRO limits', 'EVENT_UPGRADE', 3.00, 'USD');

-- 7. Cleanup the old price column from plans table once data is moved
-- (Optional: Keeping it for a bit for backward compatibility or just Drop it)
-- ALTER TABLE plans DROP COLUMN price; 
