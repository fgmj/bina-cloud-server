CREATE TABLE IF NOT EXISTS usuario_dispositivo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    dispositivo_id BIGINT NOT NULL,
    data_associacao TIMESTAMP NOT NULL,
    data_remocao TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (dispositivo_id) REFERENCES dispositivos(id)
);

-- Create unique constraint for active associations
ALTER TABLE usuario_dispositivo ADD CONSTRAINT idx_usuario_dispositivo_unique 
UNIQUE (usuario_id, dispositivo_id); 