create table if not exists tariff_fleet.contractor (
    id uuid not null constraint contractor_pk PRIMARY KEY,
	name varchar(255) not null,
	active bool default true not null
);

comment on table tariff_fleet.contractor is 'Контрагент';
comment on column tariff_fleet.contractor.id is 'Идентификатор записи об контрагенте';
comment on column tariff_fleet.contractor.name is 'Наименование контрагента';
comment on column tariff_fleet.contractor.active is 'Статус активности записи';