-- Renomeia colunas existentes para embarque_*
ALTER TABLE trip_stops RENAME COLUMN cep          TO embarque_cep;
ALTER TABLE trip_stops RENAME COLUMN street       TO embarque_street;
ALTER TABLE trip_stops RENAME COLUMN neighborhood TO embarque_neighborhood;
ALTER TABLE trip_stops RENAME COLUMN number       TO embarque_number;
ALTER TABLE trip_stops RENAME COLUMN city         TO embarque_city;
ALTER TABLE trip_stops RENAME COLUMN state        TO embarque_state;
ALTER TABLE trip_stops RENAME COLUMN complement   TO embarque_complement;

-- Adiciona colunas desembarque_* (nullable — desembarque é opcional)
ALTER TABLE trip_stops ADD COLUMN desembarque_cep           VARCHAR(9);
ALTER TABLE trip_stops ADD COLUMN desembarque_street        VARCHAR(150);
ALTER TABLE trip_stops ADD COLUMN desembarque_neighborhood  VARCHAR(100);
ALTER TABLE trip_stops ADD COLUMN desembarque_number        VARCHAR(20);
ALTER TABLE trip_stops ADD COLUMN desembarque_city          VARCHAR(100);
ALTER TABLE trip_stops ADD COLUMN desembarque_state         VARCHAR(2);
ALTER TABLE trip_stops ADD COLUMN desembarque_complement    VARCHAR(150);
