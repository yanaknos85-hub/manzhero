package ru.sber.transport.integrations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.integrations.service.ContractorCacheService;

/**
 * Источник свойств для {@link ContractorCacheService}
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "contractor-block")
public class ContractorBlockProperties {
    private long durationMs;
    private boolean enabled;
}
