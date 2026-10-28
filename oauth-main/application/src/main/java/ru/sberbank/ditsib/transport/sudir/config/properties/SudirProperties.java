package ru.sberbank.ditsib.transport.sudir.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация работы с СУДИРом.
 */
@Data
@ConfigurationProperties(prefix = "sudir", ignoreInvalidFields = true)
public class SudirProperties {
    /**
     * Clock skew - "Смещение часов"
     */
    private int clockSkewSeconds;
}
