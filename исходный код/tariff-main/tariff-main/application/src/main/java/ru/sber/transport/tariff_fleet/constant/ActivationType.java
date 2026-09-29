package ru.sber.transport.tariff_fleet.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Вид осмотра
 */
@Getter
@RequiredArgsConstructor
@Schema(title = "Тип активации", description = "Тип активации")
public enum ActivationType {
    @Schema(description = "Автоматический")
    AUTO,
    @Schema(description = "Ручной")
    MANUAL
}