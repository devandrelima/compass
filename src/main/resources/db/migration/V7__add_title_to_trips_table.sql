ALTER TABLE trips ADD COLUMN title VARCHAR(100);

UPDATE trips SET title = 'Viagem Sem Título';

ALTER TABLE trips ALTER COLUMN title SET NOT NULL;
