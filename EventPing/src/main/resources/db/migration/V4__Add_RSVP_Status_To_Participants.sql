-- V4__Add_RSVP_Status_To_Participants.sql

ALTER TABLE participants ADD COLUMN IF NOT EXISTS rsvp_status VARCHAR(50) NOT NULL DEFAULT 'TENTATIVE';

