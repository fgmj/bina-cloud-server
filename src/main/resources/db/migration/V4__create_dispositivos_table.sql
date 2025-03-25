CREATE TABLE dispositivos (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo_dispositivo VARCHAR(50),
    localizacao VARCHAR(255),
    descricao TEXT,
    versao VARCHAR(50),
    ativo BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP NOT NULL,
    ultima_atualizacao TIMESTAMP,
    ultima_conexao TIMESTAMP
); 