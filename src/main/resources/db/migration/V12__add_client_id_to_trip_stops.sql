ALTER TABLE trip_stops
    ADD COLUMN client_id UUID REFERENCES clients(id) ON DELETE SET NULL;
