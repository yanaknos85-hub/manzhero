package ru.sber.transport.integrations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * OrderInfoResponse
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OrderInfoResponse {
    
    /**
     * Обработка завершена успешно
     */
    @Schema(name = "isSuccess", description = "Обработка завершена успешно")
    private Boolean isSuccess;
    
    /**
     * Get order
     */
    @Valid
    @Schema(name = "order")
    private OrderInfoResponseExtra order;
    
    @Override
    public String toString() {
        return "class OrderInfoResponse {\n" +
               "    isSuccess: " + toIndentedString(isSuccess) + "\n" +
               "    order: " + toIndentedString(order) + "\n" +
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

