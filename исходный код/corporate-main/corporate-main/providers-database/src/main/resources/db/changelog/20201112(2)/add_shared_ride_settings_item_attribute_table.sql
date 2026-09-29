create table if not exists corporate.shared_ride_settings_item_attribute (
    settings_item_id uuid not null,
    attribute_id uuid not null,
    primary key (settings_item_id, attribute_id),
    foreign key (settings_item_id) references corporate.shared_ride_settings_item (id)
        on update cascade on delete cascade,
    foreign key (attribute_id) references corporate.attribute (id)
);
COMMENT ON TABLE corporate.shared_ride_settings_item_attribute is 'Таблица связей настройка-признак сотрудника';