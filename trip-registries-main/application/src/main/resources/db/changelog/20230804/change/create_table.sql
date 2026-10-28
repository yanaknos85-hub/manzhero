create table trip_registries.registry
(
    registry_id          uuid         not null,
    contract_id          varchar(255) not null,
    organization_id       varchar(255) not null,
    start_date            varchar(255) not null,
    end_date              varchar(255) not null,
    registry_state        varchar(255) not null,
    act_number_error       varchar(255),
    act_id                varchar(255),
    locking_indicator     integer,
    upd_status            integer,
    primary key (registry_id)
);
