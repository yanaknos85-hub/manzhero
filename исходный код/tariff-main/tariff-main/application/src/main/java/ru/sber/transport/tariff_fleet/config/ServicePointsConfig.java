package ru.sber.transport.tariff_fleet.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "service-points.excel")
public record ServicePointsConfig(
        @NotNull
        DataSize maxFileSize,
        int maxRowCount
) {
}
