-- Remover tabela e sequência se existirem
DROP TABLE IF EXISTS eventos;
DROP SEQUENCE IF EXISTS evento_sequence;

-- Criar sequência para IDs
CREATE SEQUENCE evento_sequence START WITH 1 INCREMENT BY 1;

-- Criar tabela de eventos
CREATE TABLE eventos (
    id BIGINT DEFAULT NEXT VALUE FOR evento_sequence PRIMARY KEY,
    description VARCHAR(255),
    timestamp TIMESTAMP,
    device_id VARCHAR(255),
    event_type VARCHAR(255),
    additional_data VARCHAR(255),
    phone_number VARCHAR(20)
); 