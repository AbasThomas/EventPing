-- V1__Initial_Schema.sql
-- EventPing Database Schema Creation and Initial Data Seeding

-- =====================================================
-- Create Users Table
-- =====================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255),
    phone_number VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- =====================================================
-- Create Plans Table
-- =====================================================
CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    max_events_per_day INTEGER NOT NULL,
    max_participants_per_event INTEGER NOT NULL,
    reminder_channels VARCHAR(255) NOT NULL,
    price DECIMAL(19,2) NOT NULL
);

CREATE INDEX idx_plans_name ON plans(name);

-- =====================================================
-- Create Events Table
-- =====================================================
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_date_time TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    slug VARCHAR(255) UNIQUE NOT NULL,
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_events_slug ON events(slug);
CREATE INDEX idx_events_creator_id ON events(creator_id);
CREATE INDEX idx_events_status_datetime ON events(status, event_date_time);

-- =====================================================
-- Create Participants Table
-- =====================================================
CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unsubscribed BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_participants_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE INDEX idx_participants_event_id ON participants(event_id);
CREATE INDEX idx_participants_event_email ON participants(event_id, email);

-- =====================================================
-- Create Reminders Table
-- =====================================================
CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    send_at TIMESTAMP NOT NULL,
    channel VARCHAR(255) NOT NULL DEFAULT 'EMAIL',
    sent BOOLEAN NOT NULL DEFAULT false,
    sent_at TIMESTAMP,
    CONSTRAINT fk_reminders_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminders_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

CREATE INDEX idx_reminders_event_id ON reminders(event_id);
CREATE INDEX idx_reminders_participant_id ON reminders(participant_id);
-- CRITICAL INDEX for cron job performance
CREATE INDEX idx_reminders_send_at_sent ON reminders(send_at, sent) WHERE sent = false;

-- =====================================================
-- Seed Initial Data
-- =====================================================

-- Insert FREE plan
INSERT INTO plans (name, max_events_per_day, max_participants_per_event, reminder_channels, price)
VALUES ('FREE', 3, 50, 'EMAIL', 0.00);

-- Insert PRO plan
INSERT INTO plans (name, max_events_per_day, max_participants_per_event, reminder_channels, price)
VALUES ('PRO', 999, 500, 'EMAIL,WHATSAPP', 9.99);

-- =====================================================
-- Verification Comments
-- =====================================================
-- Run these queries after migration to verify:
-- SELECT * FROM plans; -- Should return 2 rows
-- \dt -- Should show 5 tables
-- \di -- Should show all indexes
