create table tariff_fleet.fuel_contract
(
    contract_id     uuid
        constraint fuel_contract_pk primary key
        references tariff_fleet.contract (id),
    contractor_id   uuid   not null,
    contractor_name varchar(50) not null,
    amount_without_vat bigint not null,
    amount_with_vat bigint not null,
    organization_id uuid not null
        constraint fuel_contract_organization_id_fk
        references tariff_fleet.organization(id),
    logo_s3_id uuid
);

comment on table tariff_fleet.fuel_contract is 'Договор Заправка топливом';
comment on column tariff_fleet.fuel_contract.contract_id is 'Идентификатор записи о договоре';
comment on column tariff_fleet.fuel_contract.contractor_id is 'Идентификатор записи о контрагенте';
comment on column tariff_fleet.fuel_contract.contractor_name is 'Наименование контрагента';
comment on column tariff_fleet.fuel_contract.amount_without_vat is 'Сумма договора (без НДС)';
comment on column tariff_fleet.fuel_contract.amount_with_vat is 'Сумма договора (с НДС)';
comment on column tariff_fleet.fuel_contract.organization_id is 'Идентификатор записи об организации заказчика/клиента';
comment on column tariff_fleet.fuel_contract.logo_s3_id is 'Идентификатор файла лого в хранилище S3';
