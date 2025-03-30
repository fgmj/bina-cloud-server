-- Drop old table
DROP TABLE IF EXISTS dispositivos;

-- Create new table with updated structure
CREATE TABLE dispositivos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    identificador VARCHAR(255) NOT NULL UNIQUE,
    data_cadastro TIMESTAMP NOT NULL,
    ultima_conexao TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE
); 