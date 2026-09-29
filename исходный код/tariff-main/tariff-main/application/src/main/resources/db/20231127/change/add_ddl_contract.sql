create table tariff_fleet.contract
(
    id            uuid         not null
        constraint contract_pk
            primary key,
    contractor_id uuid         not null,
    amount        bigint,
    employee_id   uuid         not null,
    creation_time timestamp    not null,
    start         date         not null,
    "end"         date,
    active        boolean      not null,
    number        varchar(50) not null,
    uvhd          varchar(50)
);

comment on table tariff_fleet.contract is 'Договор';

comment on column tariff_fleet.contract.id is 'Идентификатор записи';

comment on column tariff_fleet.contract.contractor_id is 'Идентификатор записи о контрагенте';

comment on column tariff_fleet.contract.amount is 'Сумма, коп.';

comment on column tariff_fleet.contract.employee_id is 'Идентификатор записи о создателе';

comment on column tariff_fleet.contract.creation_time is 'Дата и время создания';

comment on column tariff_fleet.contract.start is 'Дата начала действия';

comment on column tariff_fleet.contract."end" is 'Дата окончания действия';

comment on column tariff_fleet.contract.active is 'Флаг активности';

comment on column tariff_fleet.contract.number is 'Номер';

comment on column tariff_fleet.contract.uvhd is 'Номер договора УВХД';

create index contract_number_index
    on tariff_fleet.contract (number);

