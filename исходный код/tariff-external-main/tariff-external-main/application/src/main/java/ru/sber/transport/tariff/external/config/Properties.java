package ru.sber.transport.tariff.external.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.tariff.external.providers.tariff.config.ExchangeProperties;

/**
 * Свойства для запроса к провайдеру данных
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "exchange")
public class Properties implements ExchangeProperties {

    /**
     * Свойства для запроса
     */
    private RequestPropertiesData request;

}
