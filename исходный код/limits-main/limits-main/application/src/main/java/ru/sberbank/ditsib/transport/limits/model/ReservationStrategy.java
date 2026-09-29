package ru.sberbank.ditsib.transport.limits.model;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Стратегия резервирования
 */
@Schema(description = "Стратегия резервирования")
public enum ReservationStrategy {

    /**
     * Все или ничего!
     */
    @Schema(description = "Все или ничего!")
    ALL,

    /**
     * Хотя бы один
     */
    @Schema(description = "Хотя бы один")
    ANY
}
