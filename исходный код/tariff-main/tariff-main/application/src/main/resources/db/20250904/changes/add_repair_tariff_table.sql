DROP TABLE IF EXISTS tariff_fleet.repair_tariff;
CREATE TABLE IF NOT EXISTS tariff_fleet.repair_tariff (
    tariff_id UUID PRIMARY KEY REFERENCES tariff_fleet.tariff (id),
    organization_id UUID NOT NULL CONSTRAINT repair_tariff_organization_id_fk REFERENCES tariff_fleet.organization (id),
    is_field_service BOOLEAN NOT NULL,
    hour_normalized_price INT NOT NULL,
    detail_discount_price DECIMAL(5,2) NOT NULL,
    work_warranty INT NOT NULL,
    mileage_warranty INT NOT NULL,
    detail_warranty INT NOT NULL
);

comment on table tariff_fleet.repair_tariff is 'Тарифы на ремонт';
comment on column tariff_fleet.repair_tariff.tariff_id is 'Идентификатор записи о тарифе';
comment on column tariff_fleet.repair_tariff.organization_id is 'Идентификатор записи об организации контрагента';
comment on column tariff_fleet.repair_tariff.is_field_service is 'Выездной сервис';
comment on column tariff_fleet.repair_tariff.hour_normalized_price is 'Стоимость нормо-часа, руб';
comment on column tariff_fleet.repair_tariff.detail_discount_price is 'Размер скидки на запасные части, %';
comment on column tariff_fleet.repair_tariff.work_warranty is 'Гарантия на работы по времени, месяцы';
comment on column tariff_fleet.repair_tariff.mileage_warranty is 'Гарантия на работы по пробегу, км';
comment on column tariff_fleet.repair_tariff.detail_warranty is 'Гарантия на запчасти, месяцы';

CREATE INDEX IF NOT EXISTS repair_tariff_organization_id_index
    ON tariff_fleet.repair_tariff (organization_id);
