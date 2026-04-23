ALTER TABLE trip_stops ADD COLUMN priority VARCHAR(20);

UPDATE trip_stops SET priority = 'NORMAL';

ALTER TABLE trip_stops ALTER COLUMN priority SET NOT NULL;