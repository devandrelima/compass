ALTER TABLE trip_stops 
ADD COLUMN stop_type VARCHAR(20);

UPDATE trip_stops 
SET stop_type = 'PRODUCT';

ALTER TABLE trip_stops 
ALTER COLUMN stop_type SET NOT NULL;