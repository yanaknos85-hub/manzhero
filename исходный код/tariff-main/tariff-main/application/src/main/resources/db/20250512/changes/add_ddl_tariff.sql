drop table tariff_fleet.tariff;

create table tariff_fleet.tariff
(
    id                uuid        not null
        constraint tariff_pk primary key,
    contract_id       uuid        not null
        constraint tariff_contract_id_fk
            references tariff_fleet.contract (id),
    active            boolean     not null,
    activation_type   varchar(10) not null,
    human_readable_id varchar(17) not null,
    creator_user_id   uuid
        constraint tariff_creator_user_id_fk
            references tariff_fleet.employee (user_id),
    creation_time     timestamp   not null
);

comment on table tariff_fleet.tariff is 'Тариф';

comment on column tariff_fleet.tariff.id is 'Идентификатор записи о тарифе';

comment on column tariff_fleet.tariff.contract_id is 'Идентификатор записи о договоре';

comment on column tariff_fleet.tariff.active is 'Флаг активности';

comment on column tariff_fleet.tariff.activation_type is 'Тип активации';

comment on column tariff_fleet.tariff.human_readable_id is 'Человекочитаемый идентификатор';

comment on column tariff_fleet.tariff.creator_user_id is 'Идентификатор записи с таблицы corporate.user сотрудника, создавшего запись';

comment on column tariff_fleet.tariff.creation_time is 'Дата и время создания';

create index if not exists tariff_contract_id_index
    on tariff_fleet.tariff (contract_id);