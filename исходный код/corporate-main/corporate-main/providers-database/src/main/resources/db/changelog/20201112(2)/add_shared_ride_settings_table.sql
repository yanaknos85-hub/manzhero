create table if not exists corporate.shared_ride_settings (
    id uuid not null,
    organization_id uuid not null,
    transport_type varchar(255) not null,
    primary key (id),
    unique (transport_type, organization_id)
 );
COMMENT ON TABLE corporate.shared_ride_settings is 'Таблица настроек совместных поездок';
COMMENT ON COLUMN corporate.shared_ride_settings.id is 'Уникальный идентификатор настроек (первичный ключ)';
COMMENT ON COLUMN corporate.shared_ride_settings.organization_id is 'Уникальный идентификатор организации';
COMMENT ON COLUMN corporate.shared_ride_settings.transport_type is 'Способ передвижения (enum)';
