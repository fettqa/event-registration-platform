ALTER TABLE events
ADD CONSTRAINT uq_events_name UNIQUE (name);
