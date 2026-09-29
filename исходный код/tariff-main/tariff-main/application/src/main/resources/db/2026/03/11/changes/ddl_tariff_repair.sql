alter table if exists tariff_fleet.repair_tariff
add column if not exists department_id uuid;

alter table if exists tariff_fleet.repair_tariff
add constraint fk_repair_tariff_department_id foreign key (department_id)
references tariff_fleet.department (id);