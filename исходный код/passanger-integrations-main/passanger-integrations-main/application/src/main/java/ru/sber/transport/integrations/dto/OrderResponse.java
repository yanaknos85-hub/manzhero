package ru.sber.transport.integrations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OrderResponse
 *
 * @param isSuccess Обработка завершена успешно
 * @param orderPartnerId Идентификатор созданного заказа ответа
 * @param orderSberTransportId
 */
public record OrderResponse(
        @Schema(name = "isSuccess", description = "Обработка завершена успешно")
        Boolean isSuccess,
        @Schema(name = "orderPartnerId", description = "Идентификатор созданного заказа ответа")
        @JsonProperty("orderParthnerId")
        String orderPartnerId,
        @JsonProperty("orderSbertransportId")
        String orderSberTransportId

) {
}

