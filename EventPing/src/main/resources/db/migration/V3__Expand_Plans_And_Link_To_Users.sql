-- V3__Expand_Plans_And_Link_To_Users.sql

-- Add feature flag columns to plans table
ALTER TABLE plans ADD COLUMN has_custom_intervals BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE plans ADD COLUMN has_analytics BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE plans ADD COLUMN has_custom_branding BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE plans ADD COLUMN has_advanced_rsvp BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE plans ADD COLUMN has_api_access BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE plans ADD COLUMN max_team_members INTEGER NOT NULL DEFAULT 0;

-- Clear old plans to ensure clean state for new tiers
DELETE FROM plans;

-- Seed the 5 distinct plan tiers
-- FREE: Basic features, email only
INSERT INTO plans (name, max_events_per_day, max_participants_per_event, reminder_channels, price, has_custom_intervals, has_analytics, has_custom_branding, has_advanced_rsvp, has_api_access, max_team_members)
VALUES ('FREE', 3, 20, 'EMAIL', 0.00, false, false, false, false, false, 0);

-- BASIC: More events, higher capacity, Telegram added
INSERT INTO plans (name, max_events_per_day, max_participants_per_event, reminder_channels, price, has_custom_intervals, has_analytics, has_custom_branding, has_advanced_rsvp, has_api_access, max_team_members)
VALUES ('BASIC', 10, 100, 'EMAIL,TELEGRAM', 9.99, true, false, false, false, false, 0);

-- PRO: Higher limits, WhatsApp added, basic analytics
INSERT INTO plans (name, max_events_per_day, max_participants_per_event, reminder_channels, price, has_custom_intervals, has_analytics, has_custom_branding, has_advanced_rsvp, has_api_access, max_team_members)
VALUES ('PRO', 50, 500, 'EMAIL,TELEGRAM,WHATSAPP', 29.99, true, true, false, true, false, 0);

-- BUSINESS: Unlimited events, high capacity, Discord added, Team access
INSERT INTO plans (name, max_events_per_day, max_participants_per_event, reminder_channels, price, has_custom_intervals, has_analytics, has_custom_branding, has_advanced_rsvp, has_api_access, max_team_members)
VALUES ('BUSINESS', 9999, 2000, 'EMAIL,TELEGRAM,WHATSAPP,DISCORD', 79.99, true, true, true, true, false, 5);

-- ENTERPRISE: Everything unlimited, custom branding, API access
INSERT INTO plans (name, max_events_per_day, max_participants_per_event, reminder_channels, price, has_custom_intervals, has_analytics, has_custom_branding, has_advanced_rsvp, has_api_access, max_team_members)
VALUES ('ENTERPRISE', 99999, 99999, 'EMAIL,TELEGRAM,WHATSAPP,DISCORD', 249.99, true, true, true, true, true, 999);

-- Link users to plans
ALTER TABLE users ADD COLUMN plan_id BIGINT;
-- Set existing users to FREE plan
UPDATE users SET plan_id = (SELECT id FROM plans WHERE name = 'FREE');
-- Make plan_id non-nullable after seeding
-- ALTER TABLE users ALTER COLUMN plan_id SET NOT NULL; -- Optional, depends on registration flow

ALTER TABLE users ADD CONSTRAINT fk_users_plan FOREIGN KEY (plan_id) REFERENCES plans(id);
