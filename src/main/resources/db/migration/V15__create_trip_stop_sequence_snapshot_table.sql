CREATE TABLE trip_stop_sequence_snapshot (
    id                         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id                    UUID      NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    stop_id                    UUID      NOT NULL REFERENCES trip_stops(id) ON DELETE CASCADE,
    embarque_sequence_order    INTEGER   NOT NULL,
    desembarque_sequence_order INTEGER,
    created_at                 TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trip_stop_sequence_snapshot_trip_id ON trip_stop_sequence_snapshot (trip_id);
