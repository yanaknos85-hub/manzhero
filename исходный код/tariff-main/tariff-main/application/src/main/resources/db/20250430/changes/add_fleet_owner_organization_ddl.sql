create table if not exists tariff_fleet.fleet_owner_organization
(
    organization_id uuid not null
        constraint fleet_owner_organization_organization_id_fk
            references tariff_fleet.organization
);

comment on table tariff_fleet.fleet_owner_organization is 'Организация владельца автопарка';

comment on column tariff_fleet.fleet_owner_organization.organization_id is 'Идентификатор записи об организации';

create unique index if not exists fleet_owner_organization_organization_id_uindex
    on tariff_fleet.fleet_owner_organization (organization_id);