create table tariff_fleet.ewb_contract
(
    contract_id                     uuid
        constraint ewb_contract_pk primary key
        references tariff_fleet.contract (id),
    contractor_organization_id      uuid        not null
        constraint ewb_contract_contractor_organization_id_fk
            references tariff_fleet.organization (id),
    amount                          bigint      not null,
    inspection_type                 varchar(50) not null,
    edf_operator_id                 varchar(10) not null
        constraint ewb_contract_edf_operator_id_fk
            references tariff_fleet.edf_operator (id),
    edf_code                        varchar(50) not null,
    organization_medical_license_id uuid
        constraint ewb_contract_organization_medical_license_id_fk
            references tariff_fleet.organization_medical_license (id)
);

comment on table tariff_fleet.ewb_contract is 'Договор ЭПЛ';

comment on column tariff_fleet.ewb_contract.contract_id is 'Идентификатор записи о договоре';

comment on column tariff_fleet.ewb_contract.contractor_organization_id is 'Идентификатор записи об организации контрагента';

comment on column tariff_fleet.ewb_contract.amount is 'Сумма договора (без НДС)';

comment on column tariff_fleet.ewb_contract.inspection_type is 'Вид осмотра';

comment on column tariff_fleet.ewb_contract.edf_operator_id is 'Идентификатор записи об операторе ЭДО';

comment on column tariff_fleet.ewb_contract.edf_code is 'Код участника';

comment on column tariff_fleet.ewb_contract.organization_medical_license_id is 'Идентификатор записи об медицинской лицензии организации';

create index if not exists ewb_contract_edf_operator_id_index
    on tariff_fleet.ewb_contract (edf_operator_id);

create index if not exists ewb_contract_organization_medical_license_id_index
    on tariff_fleet.ewb_contract (organization_medical_license_id);