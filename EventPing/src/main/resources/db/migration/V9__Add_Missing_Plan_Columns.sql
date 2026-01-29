-- V9__Add_Missing_Plan_Columns.sql
ALTER TABLE plans ADD COLUMN IF NOT EXISTS has_custom_templates BOOLEAN NOT NULL DEFAULT false;
