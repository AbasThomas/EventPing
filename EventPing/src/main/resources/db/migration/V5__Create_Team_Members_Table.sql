-- V5__Create_Team_Members_Table.sql

CREATE TABLE team_members (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    CONSTRAINT fk_team_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(owner_id, user_id)
);

CREATE INDEX idx_team_owner ON team_members(owner_id);
CREATE INDEX idx_team_user ON team_members(user_id);
