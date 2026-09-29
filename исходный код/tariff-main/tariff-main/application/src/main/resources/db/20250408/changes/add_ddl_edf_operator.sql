create table tariff_fleet.edf_operator
(
    id     varchar(10)
        constraint edf_operator_pk
            primary key,
    name   varchar(50) not null,
    title  varchar(50) not null,
    active boolean     not null
);

comment on table tariff_fleet.edf_operator is 'Оператор ЭДО';

comment on column tariff_fleet.edf_operator.id is 'Идентификатор записи об операторе ЭДО';

comment on column tariff_fleet.edf_operator.name is 'Наименование';

comment on column tariff_fleet.edf_operator.title is 'Отображаемое наименование';

comment on column tariff_fleet.edf_operator.active is 'Флаг активности';

