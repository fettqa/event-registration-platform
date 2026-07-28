-- Add profile name and SUPER_USER role; track event creators.
ALTER TABLE users ADD COLUMN full_name VARCHAR(255);

UPDATE users SET full_name = 'System Admin' WHERE full_name IS NULL;

ALTER TABLE users ALTER COLUMN full_name SET NOT NULL;

ALTER TABLE users DROP CONSTRAINT chk_users_role;
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN', 'SUPER_USER'));

ALTER TABLE events ADD COLUMN created_by_id BIGINT;
ALTER TABLE events ADD CONSTRAINT fk_events_created_by
    FOREIGN KEY (created_by_id) REFERENCES users (id);
