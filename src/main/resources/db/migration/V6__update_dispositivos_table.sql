-- Drop existing columns that are no longer needed
ALTER TABLE dispositivos DROP COLUMN IF EXISTS tipo_dispositivo;
ALTER TABLE dispositivos DROP COLUMN IF EXISTS localizacao;
ALTER TABLE dispositivos DROP COLUMN IF EXISTS descricao;
ALTER TABLE dispositivos DROP COLUMN IF EXISTS versao;
ALTER TABLE dispositivos DROP COLUMN IF EXISTS ultima_atualizacao;

-- Add new columns if they don't exist
ALTER TABLE dispositivos ADD COLUMN IF NOT EXISTS identificador VARCHAR(255) UNIQUE NOT NULL;

-- Update existing records with a default identificador
UPDATE dispositivos SET identificador = 'DEVICE_' || id WHERE identificador IS NULL; 