create table if not exists tariff_fleet.medic_organization
(
    organization_id uuid not null
        constraint medic_organization_organization_id_fk
            references tariff_fleet.organization
);

comment on table tariff_fleet.medic_organization is 'Организация, осуществляющая медицинский осмотр';

comment on column tariff_fleet.medic_organization.organization_id is 'Идентификатор записи об организации';

create unique index if not exists medic_organization_organization_id_uindex
    on tariff_fleet.medic_organization (organization_id);

