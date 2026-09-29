package ru.sber.transport.tariff.external.config;

import ru.sber.transport.tariff.external.providers.tariff.config.RequestProperties;

/**
 * Настройки запроса внешних данных
 *
 * @param apiKey ключ для запроса
 * @param clientId идентификатор клиента
 * @param baseUrl базовый адрес для запроса
 */
public record RequestPropertiesData(String apiKey, String clientId, String baseUrl) implements RequestProperties {
}
