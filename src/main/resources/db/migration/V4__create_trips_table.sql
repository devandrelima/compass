CREATE TABLE trips (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status                   VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    user_id                  UUID NOT NULL REFERENCES users(id),
    origin_cep               VARCHAR(9)   NOT NULL,
    origin_street            VARCHAR(150) NOT NULL,
    origin_neighborhood      VARCHAR(100) NOT NULL,
    origin_number            VARCHAR(20)  NOT NULL,
    origin_city              VARCHAR(100) NOT NULL,
    origin_state             VARCHAR(2)   NOT NULL,
    origin_complement        VARCHAR(150),

    destination_cep          VARCHAR(9)   NOT NULL,
    destination_street       VARCHAR(150) NOT NULL,
    destination_neighborhood VARCHAR(100) NOT NULL,
    destination_number       VARCHAR(20)  NOT NULL,
    destination_city         VARCHAR(100) NOT NULL,
    destination_state        VARCHAR(2)   NOT NULL,
    destination_complement   VARCHAR(150),

    created_at               TIMESTAMP NOT NULL DEFAULT NOW()
);