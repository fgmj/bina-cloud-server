-- Exemplo de dados para o dashboard
-- Período: Últimos 10 dias (2025-03-08 a 2025-03-17)
-- Total de registros: 42
-- Distribuição: ~70% atendidas, ~30% perdidas

-- Dia 1: 2025-03-08 (4 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990001', '2025-03-08 09:15:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990002', '2025-03-08 10:30:00', 'device-001', 'MISSED', 'Chamada perdida'),
('11999990003', '2025-03-08 14:45:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990004', '2025-03-08 16:20:00', 'device-002', 'ANSWERED', 'Chamada atendida');

-- Dia 2: 2025-03-09 (3 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990005', '2025-03-09 11:10:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990006', '2025-03-09 13:25:00', 'device-002', 'MISSED', 'Chamada perdida'),
('11999990007', '2025-03-09 15:40:00', 'device-001', 'ANSWERED', 'Chamada atendida');

-- Dia 3: 2025-03-10 (4 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990008', '2025-03-10 10:05:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990009', '2025-03-10 12:30:00', 'device-001', 'MISSED', 'Chamada perdida'),
('11999990010', '2025-03-10 14:15:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990011', '2025-03-10 16:45:00', 'device-001', 'ANSWERED', 'Chamada atendida');

-- Dia 4: 2025-03-11 (3 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990012', '2025-03-11 09:20:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990013', '2025-03-11 11:35:00', 'device-002', 'MISSED', 'Chamada perdida'),
('11999990014', '2025-03-11 15:50:00', 'device-002', 'ANSWERED', 'Chamada atendida');

-- Dia 5: 2025-03-12 (4 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990015', '2025-03-12 10:25:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990016', '2025-03-12 12:40:00', 'device-002', 'MISSED', 'Chamada perdida'),
('11999990017', '2025-03-12 14:55:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990018', '2025-03-12 16:10:00', 'device-002', 'ANSWERED', 'Chamada atendida');

-- Dia 6: 2025-03-13 (3 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990019', '2025-03-13 09:30:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990020', '2025-03-13 11:45:00', 'device-001', 'MISSED', 'Chamada perdida'),
('11999990021', '2025-03-13 15:00:00', 'device-001', 'ANSWERED', 'Chamada atendida');

-- Dia 7: 2025-03-14 (4 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990022', '2025-03-14 10:15:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990023', '2025-03-14 12:30:00', 'device-002', 'MISSED', 'Chamada perdida'),
('11999990024', '2025-03-14 14:45:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990025', '2025-03-14 16:00:00', 'device-001', 'ANSWERED', 'Chamada atendida');

-- Dia 8: 2025-03-15 (3 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990026', '2025-03-15 09:40:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990027', '2025-03-15 11:55:00', 'device-001', 'MISSED', 'Chamada perdida'),
('11999990028', '2025-03-15 15:10:00', 'device-001', 'ANSWERED', 'Chamada atendida');

-- Dia 9: 2025-03-16 (4 eventos)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990029', '2025-03-16 10:35:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990030', '2025-03-16 12:50:00', 'device-002', 'MISSED', 'Chamada perdida'),
('11999990031', '2025-03-16 15:05:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990032', '2025-03-16 16:20:00', 'device-001', 'ANSWERED', 'Chamada atendida');

-- Dia 10: 2025-03-17 (10 eventos - dia atual com mais atividade)
INSERT INTO eventos (phone_number, timestamp, device_id, event_type, description) VALUES
('11999990033', '2025-03-17 09:00:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990034', '2025-03-17 10:15:00', 'device-002', 'MISSED', 'Chamada perdida'),
('11999990035', '2025-03-17 11:30:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990036', '2025-03-17 12:45:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990037', '2025-03-17 13:00:00', 'device-001', 'MISSED', 'Chamada perdida'),
('11999990038', '2025-03-17 14:15:00', 'device-002', 'ANSWERED', 'Chamada atendida'),
('11999990039', '2025-03-17 15:30:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990040', '2025-03-17 16:45:00', 'device-002', 'MISSED', 'Chamada perdida'),
('11999990041', '2025-03-17 17:00:00', 'device-001', 'ANSWERED', 'Chamada atendida'),
('11999990042', '2025-03-17 18:15:00', 'device-002', 'ANSWERED', 'Chamada atendida'); 