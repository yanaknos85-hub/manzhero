create table if not exists corporate.shared_ride_settings_item (
    id uuid not null,
    shared_ride_settings_id uuid,
    setting_type varchar(255),
    primary key (id),
    foreign key (shared_ride_settings_id) references corporate.shared_ride_settings (id)
        on delete cascade on update cascade
);
COMMENT ON TABLE corporate.shared_ride_settings_item is 'Таблица элемента настройки совместных поездок';
COMMENT ON COLUMN corporate.shared_ride_settings_item.id is 'Уникальный идентификатор элемента (первичный ключ)';
COMMENT ON COLUMN corporate.shared_ride_settings_item.shared_ride_settings_id is
'Уникальный идентификатор настроек (внешний ключ)';
COMMENT ON COLUMN corporate.shared_ride_settings_item.setting_type is 'Тип настройки (enum)';


