package ru.sber.transport.integrations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * CancelOrderResponse
 */

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class CancelOrderResponse {
    /**
     * Обработка завершена успешно
     */
    @Schema(name = "isSuccess", description = "Обработка завершена успешно")
    private Boolean isSuccess;
    
    /**
     * Номер заказа
     */
    @Schema(name = "orderPartnerId", description = "Номер заказа")
    @JsonProperty("orderParthnerID")
    private String orderPartnerId;
    
    @Override
    public String toString() {
        return "class CancelOrderResponse {\n" +
               "    isSuccess: " + toIndentedString(isSuccess) + "\n" +
               "    orderPartnerId: " + toIndentedString(orderPartnerId) + "\n" +
               "}";
    }
    
    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

