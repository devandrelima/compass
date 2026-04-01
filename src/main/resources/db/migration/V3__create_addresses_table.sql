CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    cep VARCHAR(9) NOT NULL,
    street VARCHAR(150) NOT NULL,
    neighborhood VARCHAR(100) NOT NULL,
    number VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    complement VARCHAR(150) NOT NULL,

    client_id UUID NOT NULL,

    CONSTRAINT fk_address_client
       FOREIGN KEY (client_id)
           REFERENCES clients(id)
           ON DELETE CASCADE
);