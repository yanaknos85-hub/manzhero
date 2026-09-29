create table if not exists corporate.shared_ride_settings_item_employee (
    settings_item_id uuid not null,
    employee_id uuid not null,
    primary key (settings_item_id, employee_id),
    foreign key (settings_item_id) references corporate.shared_ride_settings_item (id)
        on update cascade on delete cascade,
    foreign key (employee_id) references corporate.employee (id)
);
COMMENT ON TABLE corporate.shared_ride_settings_item_employee is 'Таблица связей настройка-сотрудник';