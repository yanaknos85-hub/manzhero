create table if not exists tariff_fleet.technic_organization
(
    organization_id uuid not null
        constraint technic_organization_organization_id_fk
            references tariff_fleet.organization
);

comment on table tariff_fleet.technic_organization is 'Организация, осуществляющая технический осмотр';

comment on column tariff_fleet.technic_organization.organization_id is 'Идентификатор записи об организации';

create unique index if not exists technic_organization_organization_id_uindex
    on tariff_fleet.technic_organization (organization_id);