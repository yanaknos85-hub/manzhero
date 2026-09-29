create table tariff_fleet.repair_contract
(
    contract_id     uuid
        constraint repair_contract_pk primary key
        references tariff_fleet.contract (id),
    contractor_id   uuid   not null,
    amount          bigint not null,
    contractor_name varchar(50) not null
);

comment on table tariff_fleet.repair_contract is 'Договор Ремонт';

comment on column tariff_fleet.repair_contract.contract_id is 'Идентификатор записи о договоре';

comment on column tariff_fleet.repair_contract.contractor_id is 'Идентификатор записи о контрагенте';

comment on column tariff_fleet.repair_contract.amount is 'Сумма договора (без НДС)';

comment on column tariff_fleet.repair_contract.contractor_name is 'Наименование контрагента';