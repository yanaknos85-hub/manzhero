/**
 * Модуль бизнес-логики
 */
open module tariff.external.business {
    exports ru.sber.transport.tariff.external.business;
    exports ru.sber.transport.tariff.external.business.impl;
    requires tariff.external.providers;
    requires tariff.external.model;
    requires static lombok;
}