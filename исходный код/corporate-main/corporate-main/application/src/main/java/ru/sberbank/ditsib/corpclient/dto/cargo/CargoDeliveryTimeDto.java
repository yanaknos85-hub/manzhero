package ru.sberbank.ditsib.corpclient.dto.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTimeUrgency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Entity of delivery time.
 */
@Data
@Schema(title = "Срок доставки", description = "Данные срока доставки в зависимости от длины маршрута")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CargoDeliveryTimeDto {
    
    /**
     * Identifier
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Label
     */
    @NotBlank
    @Schema(description = "Описание срока доставки", maxLength = 255)
    @Size(max = 255)
    private String label;
    
    /**
     * Tariff urgency
     */
    @NotNull
    @Schema(description = "Срочность доставки")
    private CargoDeliveryTimeUrgency urgency;
    
    /**
     * Start of interval
     */
    @NotNull
    @Schema(description = "Расстояние 'от'")
    private Integer start;
    
    /**
     * End of interval
     */
    @NotNull
    @Schema(description = "Расстояние 'до'")
    private Integer end;
    
    /**
     * Default delivery time
     */
    @NotNull
    @Schema(description = "Срок доставки по-умолчанию")
    private Integer defaultValue;
    
    /**
     * Delivery time
     */
    @NotNull
    @Schema(description = "Срок доставки фактический")
    private Integer value;
}
