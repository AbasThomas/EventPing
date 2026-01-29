-- V10__Add_Event_Custom_Fields_And_Integrations.sql
-- Add support for custom registration fields and event integrations

-- =====================================================
-- Add registration_enabled to Events Table
-- =====================================================
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'events' AND column_name = 'registration_enabled'
    ) THEN
        ALTER TABLE events ADD COLUMN registration_enabled BOOLEAN NOT NULL DEFAULT true;
    END IF;
END $$;

-- =====================================================
-- Create Event Custom Fields Table
-- =====================================================
CREATE TABLE IF NOT EXISTS event_custom_fields (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    field_type VARCHAR(50) NOT NULL, -- TEXT, EMAIL, PHONE, SELECT, CHECKBOX
    is_required BOOLEAN NOT NULL DEFAULT false,
    placeholder_text VARCHAR(255),
    field_options TEXT, -- Comma-separated options for SELECT type
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_custom_fields_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_custom_fields_event_id ON event_custom_fields(event_id);
CREATE INDEX IF NOT EXISTS idx_custom_fields_display_order ON event_custom_fields(event_id, display_order);

-- =====================================================
-- Create Registration Responses Table
-- =====================================================
CREATE TABLE IF NOT EXISTS registration_responses (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL,
    custom_field_id BIGINT NOT NULL,
    response_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_responses_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE,
    CONSTRAINT fk_responses_custom_field FOREIGN KEY (custom_field_id) REFERENCES event_custom_fields(id) ON DELETE CASCADE,
    CONSTRAINT unique_participant_field UNIQUE (participant_id, custom_field_id)
);

CREATE INDEX IF NOT EXISTS idx_responses_participant_id ON registration_responses(participant_id);
CREATE INDEX IF NOT EXISTS idx_responses_custom_field_id ON registration_responses(custom_field_id);

-- =====================================================
-- Create Event Integrations Table
-- =====================================================
CREATE TABLE IF NOT EXISTS event_integrations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    integration_type VARCHAR(50) NOT NULL, -- EMAIL, WHATSAPP, TELEGRAM, DISCORD, SLACK, SMS
    configuration JSONB, -- For future OAuth tokens, webhooks, etc.
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_integrations_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT unique_event_integration UNIQUE (event_id, integration_type)
);

CREATE INDEX IF NOT EXISTS idx_integrations_event_id ON event_integrations(event_id);
CREATE INDEX IF NOT EXISTS idx_integrations_type ON event_integrations(integration_type);
