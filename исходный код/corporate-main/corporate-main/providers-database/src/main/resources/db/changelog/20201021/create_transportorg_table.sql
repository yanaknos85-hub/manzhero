CREATE TABLE IF NOT EXISTS corporate.transport_org
(
    id               UUID PRIMARY KEY,
    organization_id  UUID NOT NULL,
    transport_type   VARCHAR NOT NULL
);

COMMENT ON TABLE  corporate.transport_org is 'Таблица типов транспорта';
COMMENT ON COLUMN corporate.transport_org.organization_id is 'Номер организации (первичный ключ)';
COMMENT ON COLUMN corporate.transport_org.transport_type is 'Тип транспорта';
