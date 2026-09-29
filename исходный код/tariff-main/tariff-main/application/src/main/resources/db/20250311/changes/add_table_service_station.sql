create table if not exists tariff_fleet.service_station
(
    id uuid primary key,
    name varchar(50) not null,
    address varchar(70) not null,
    latitude numeric(8, 6) not null,
    longitude numeric(9, 6) not null,
    logo_s3_id uuid,
    contract_id uuid not null
        constraint fk_service_station_contract
            references tariff_fleet.contract(id)
);

comment on table tariff_fleet.service_station is 'Автосервис';
comment on column tariff_fleet.service_station.id is 'Идентификатор записи об автосервисе';
comment on column tariff_fleet.service_station.name is 'Наименование автосервиса';
comment on column tariff_fleet.service_station.address is 'Адрес автосервиса';
comment on column tariff_fleet.service_station.latitude is 'Широта';
comment on column tariff_fleet.service_station.longitude is 'Долгота';
comment on column tariff_fleet.service_station.logo_s3_id is 'Идентификатор файла в хранилище S3';
comment on column tariff_fleet.service_station.contract_id is 'Идентификатор записи договора';