alter table tariff_fleet.contract
    drop column contractor_id;
alter table tariff_fleet.contract
    drop column contractor_name;
alter table tariff_fleet.contract
    drop column amount;
alter table tariff_fleet.contract
    drop column active;
alter table tariff_fleet.contract
    add column creator_user_id uuid
        constraint contract_employee_user_id_fk references tariff_fleet.employee (user_id);
DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'tariff_fleet'
                  and table_name = 'contract'
            ) THEN
            update tariff_fleet.contract tc
            set creator_user_id = (select te.user_id
                                   from tariff_fleet.employee te
                                   where te.id = tc.employee_id
                                   limit 1);
        END IF;
    END
$do$;

alter table tariff_fleet.contract
    drop column employee_id;
create index if not exists contract_creator_user_id_index
    on tariff_fleet.contract (creator_user_id);

comment on column tariff_fleet.contract.creator_user_id is 'Идентификатор записи с таблицы corporate.user сотрудника, создавшего запись';