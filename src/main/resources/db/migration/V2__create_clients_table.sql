CREATE TABLE clients (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     name VARCHAR(100) NOT NULL,

     user_id UUID NOT NULL,

     CONSTRAINT fk_client_user
         FOREIGN KEY (user_id)
             REFERENCES users(id)
             ON DELETE CASCADE
);

CREATE INDEX idx_clients_user_id ON clients(user_id);