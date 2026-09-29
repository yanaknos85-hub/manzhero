alter table tariff_fleet.repair_tariff
alter column department_id drop not null;

update tariff_fleet.repair_tariff
set department_id = null;