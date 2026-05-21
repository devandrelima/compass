ALTER TABLE trip_stops
    ADD COLUMN embarque_sequence_order  INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN desembarque_sequence_order INTEGER;

UPDATE trip_stops
SET embarque_sequence_order = sequence_order;

ALTER TABLE trip_stops DROP COLUMN sequence_order;
