package ru.sber.transport.integrations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.integrations.constant.StatusCode;

import java.util.UUID;

/**
 * @param requestId ID заявки в АС СберТранспорт
 * @param humanReadableId Человекочитаемый ID заявки в АС СберТранспорт
 * @param inn
 * @param statusCode
 * @param workGroup
 * @param tariff Идентификатор тарифа
 * @param planStartTime Плановое время начала поездки
 * @param routePoints
 * @param propertyClass
 * @param comment Дополнительная информация
 * @param expected
 * @param passengerCount Кол-во пассажиров
 * @param information
 */
public record OrderRequest(
        @Schema(name = "requestId", description = "ID заявки в АС СберТранспорт")
        String requestId,
        @Schema(name = "humanReadableId", description = "Человекочитаемый ID заявки в АС СберТранспорт")
        String humanReadableId,
        String inn,
        StatusCode statusCode,
        String workGroup,
        @Schema(name = "tariff", description = "Идентификатор тарифа")
        Integer tariff,
        @Schema(name = "planStartTime", description = "Плановое время начала поездки")
        String planStartTime,
        OrderRoutePoints routePoints,
        @JsonProperty("class")
        String propertyClass,
        @Schema(name = "comment", description = "Дополнительная информация")
        String comment,
        Expected expected,
        @Schema(name = "passengerCount", description = "Кол-во пассажиров")
        Integer passengerCount,
        GroupTransferRequestInformation information
) {
    
    /**
     * @param vehicleId Ид транспорта
     * @param time Предполагаемая длительность поездки, в секундах
     * @param distance Предполагаемая дистанция поездки, в км
     */
    public record Expected(
            UUID vehicleId,
            Long time,
            Double distance
    ) {
    }
}

