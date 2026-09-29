create table tariff_fleet.organization_medical_license
(
    id          uuid        not null
        constraint organization_medical_license_pk
            primary key,
    series      varchar(50) not null,
    number      varchar(50) not null
);

comment on table tariff_fleet.organization_medical_license is 'Медицинская лицензия организации';

comment on column tariff_fleet.organization_medical_license.id is 'Идентификатор записи о медицинской лицензии организации';

comment on column tariff_fleet.organization_medical_license.series is 'Серия';

comment on column tariff_fleet.organization_medical_license.number is 'Номер';