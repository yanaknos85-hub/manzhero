package ru.sber.transport.tariff.external.providers.tariff.config;

/**
 * Свойства для запроса к провайдеру тарифов.
 */
public interface ExchangeProperties {

    /**
     * Настройки запроса
     *
     * @return настройки запроса
     */
    RequestProperties getRequest();
}
