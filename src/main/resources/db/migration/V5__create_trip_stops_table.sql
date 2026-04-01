CREATE TABLE trip_stops (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id        UUID        NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    sequence_order INT         NOT NULL,
    cep            VARCHAR(9)   NOT NULL,
    street         VARCHAR(150) NOT NULL,
    neighborhood   VARCHAR(100) NOT NULL,
    number         VARCHAR(20)  NOT NULL,
    city           VARCHAR(100) NOT NULL,
    state          VARCHAR(2)   NOT NULL,
    complement     VARCHAR(150),
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trip_stops_trip_id ON trip_stops (trip_id);
