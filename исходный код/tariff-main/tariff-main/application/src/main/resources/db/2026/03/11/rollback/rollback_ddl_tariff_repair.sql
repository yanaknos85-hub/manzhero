alter table tariff_fleet.repair_tariff
drop constraint if exists fk_repair_tariff_department_id;

alter table tariff_fleet.repair_tariff
drop column if exists department_id;