delete from tariff_fleet.fleet_owner_organization;

alter table tariff_fleet.fleet_owner_organization
    add constraint fleet_owner_organization_pk
        primary key (organization_id);

alter table tariff_fleet.fleet_owner_organization
    add edf_operator_id varchar(10) not null;

comment on column tariff_fleet.fleet_owner_organization.edf_operator_id is 'Идентификатор записи об операторе ЭДО';

alter table tariff_fleet.fleet_owner_organization
    add edf_code varchar(50) not null;

comment on column tariff_fleet.fleet_owner_organization.edf_code is 'Код участника';

create index fleet_owner_organization_edf_id_index
    on tariff_fleet.fleet_owner_organization (edf_operator_id);

alter table tariff_fleet.fleet_owner_organization
    add constraint fleet_owner_organization_edf_operator_id_fk
        foreign key (edf_operator_id) references tariff_fleet.edf_operator;

