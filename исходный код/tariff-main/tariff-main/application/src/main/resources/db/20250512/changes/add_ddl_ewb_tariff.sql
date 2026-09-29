create table tariff_fleet.ewb_tariff
(
    tariff_id       uuid
        constraint ewb_tariff_pk primary key
        references tariff_fleet.tariff (id),
    organization_id uuid not null
        constraint ewb_tariff_organization_id_fk
            references tariff_fleet.organization (id),
    department_id   uuid not null
        constraint ewb_tariff_department_id_fk
            references tariff_fleet.department (id),
    amount                          bigint      not null
);

comment on table tariff_fleet.ewb_tariff is 'Тариф ЭПЛ';

comment on column tariff_fleet.ewb_tariff.tariff_id is 'Идентификатор записи о тарифе';

comment on column tariff_fleet.ewb_tariff.organization_id is 'Идентификатор записи об организации контрагента';

comment on column tariff_fleet.ewb_tariff.department_id is 'Идентификатор записи о подразделении контрагента';

comment on column tariff_fleet.ewb_contract.amount is 'Стоимость осмотра';

create index if not exists ewb_tariff_organization_id_index
    on tariff_fleet.ewb_tariff (organization_id);

create index if not exists ewb_tariff_department_id_index
    on tariff_fleet.ewb_tariff (department_id);