create table if not exists tariff_fleet.organization_contract
(
    organization_id uuid not null
        constraint fk_organization_contract_organization_id
            references tariff_fleet.organization(id),
    contract_id uuid not null
        constraint fk_contract_organization_contract_id
            references tariff_fleet.contract(id),
    primary key (organization_id, contract_id)
);

comment on table tariff_fleet.organization_contract is 'Связь организаций и договоров';
comment on column tariff_fleet.organization_contract.organization_id is 'Идентификатор организации';
comment on column tariff_fleet.organization_contract.contract_id is 'Идентификатор договора';