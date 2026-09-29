create table if not exists corporate.shared_ride_settings_item_position (
    settings_item_id uuid not null,
    position_id uuid not null,
    primary key (settings_item_id, position_id),
    foreign key (settings_item_id) references corporate.shared_ride_settings_item (id)
        on update cascade on delete cascade,
    foreign key (position_id) references corporate.position (id)
);
COMMENT ON TABLE corporate.shared_ride_settings_item_position is 'Таблица связей настройка-должность сотрудника';
