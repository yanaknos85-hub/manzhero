DROP TABLE IF EXISTS tariff_fleet.fuel_tariff;
CREATE TABLE IF NOT EXISTS tariff_fleet.fuel_tariff (
    tariff_id UUID PRIMARY KEY REFERENCES tariff_fleet.tariff (id),
    organization_id UUID NOT NULL CONSTRAINT fuel_tariff_organization_id_fk REFERENCES tariff_fleet.organization (id),
    department_id UUID NOT NULL constraint fuel_tariff_department_id_fk REFERENCES tariff_fleet.department (id),
    discount DECIMAL(5,2) NOT NULL
);

comment
    on table tariff_fleet.fuel_tariff is 'Тарифы заправка тапливом';
comment
    on column tariff_fleet.fuel_tariff.tariff_id is 'Идентификатор записи о тарифе';
comment
    on column tariff_fleet.fuel_tariff.organization_id is 'Идентификатор записи об организации контрагента';
comment
    on column tariff_fleet.fuel_tariff.department_id is 'Идентификатор подразделения';
comment
    on column tariff_fleet.fuel_tariff.discount is 'Скидка от розничной сети';


CREATE INDEX IF NOT EXISTS fuel_tariff_organization_id_idx ON tariff_fleet.fuel_tariff (organization_id);
CREATE INDEX IF NOT EXISTS fuel_tariff_department_id_idx ON tariff_fleet.fuel_tariff (department_id);