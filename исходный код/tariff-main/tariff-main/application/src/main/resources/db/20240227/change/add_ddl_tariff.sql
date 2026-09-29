create table if not exists tariff_fleet.tariff
(
    id uuid not null constraint tariff_pk primary key,
    human_readable_id varchar(17),
    hour_normalized_price int not null check (hour_normalized_price >= 0 and hour_normalized_price <= 999999),
    detail_discount_price double precision not null check (detail_discount_price >= 0 and detail_discount_price <= 100),
    work_warranty int not null check (work_warranty >= 0 and work_warranty <= 99),
    mileage_warranty int not null check (mileage_warranty >= 0 and mileage_warranty <= 99999),
    detail_warranty int not null check (detail_warranty >= 0 and detail_warranty <= 99),
    organization_id uuid not null constraint tariff_organization_id_fk references tariff_fleet.organization,
    contract_id uuid not null constraint tariff_contract_id_fk references tariff_fleet.contract,
    employee_id uuid not null constraint tariff_employee_id_fk references tariff_fleet.employee,
    creation_time timestamp not null
);

comment on table tariff_fleet.tariff is 'Тарифы';
comment on column tariff_fleet.tariff.id is 'Идентификатор тарифа';
comment on column tariff_fleet.tariff.human_readable_id is 'Читаемый идентификатор тарифа';
comment on column tariff_fleet.tariff.hour_normalized_price is 'Стоимость нормо-часа, руб';
comment on column tariff_fleet.tariff.detail_discount_price is 'Размер скидки на запасные части, %';
comment on column tariff_fleet.tariff.work_warranty is 'Гарантия на работы по времени, месяцы';
comment on column tariff_fleet.tariff.mileage_warranty is 'Гарантия на работы по пробегу, км';
comment on column tariff_fleet.tariff.detail_warranty is 'Гарантия на запчасти, месяцы';
comment on column tariff_fleet.tariff.organization_id is 'Идентификатор организации';
comment on column tariff_fleet.tariff.contract_id is 'Идентификатор договора';
comment on column tariff_fleet.tariff.employee_id is 'Пользователь, создатель записи';
comment on column tariff_fleet.tariff.creation_time is 'Время создания записи';