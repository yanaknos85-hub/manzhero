package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Перераспределение между видами транспорта", description = "Перераспределение между видами транспорта")
public class LimitReSharingByTransportTypeDTO {
    
    /**
     * Sum
     */
    @NotNull
    @Schema(description = "Сумма в копейках")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    /**
     * Limit limit
     */
    @NotNull
    @Schema(description = "Лимит")
    private UUID limitId;
    
    
    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Вид транспорта 1")
    private TransportTypeEnum sourceTransportType;
    
    
    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Вид транспорта 2")
    private TransportTypeEnum targetTransportType;
    
    @Schema(description = "Период-источник")
    private Integer fromPeriod;
    
    @Schema(description = "Период-цель")
    private Integer toPeriod;
    
}
