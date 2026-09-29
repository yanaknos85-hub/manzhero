package ru.sber.transport.tariff.external.providers.tariff.config;

public interface RequestProperties {

    /**
     * Получить ключ для запроса
     *
     * @return ключ для запроса
     */
    String apiKey();

    /**
     * Получить идентификатор клиента
     *
     * @return идентификатор клиента
     */
    String clientId();

    /**
     * Получить базовый адрес для запроса
     *
     * @return базовый адрес для запроса
     */
    String baseUrl();

}
