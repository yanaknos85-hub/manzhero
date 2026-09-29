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

import jakarta.validation.constraints.Min;
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
@Schema(title = "Перераспределение между подразделениями", description = "Перераспределение между подразделениями")
public class LimitReSharingByDepartmentDTO {
    
    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Сумма в копейках")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    /**
     * Limit limit
     */
    @NotNull
    @Schema(description = "Родительский лимит")
    private Integer year;
    
    /**
     * Limit limit
     */
    @NotNull
    @Schema(description = "Исходное подразделение")
    private UUID sourceDepartmentId;
    
    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Исходный вид транспорта")
    private TransportTypeEnum sourceTransportType;
    
    /**
     * Limit limit
     */
    @NotNull
    @Schema(description = "Целевое подразделение")
    private UUID targetDepartmentId;
    
    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Целевой вид транспорта")
    private TransportTypeEnum targetTransportType;
    
    @Schema(description = "Период-источник")
    private Integer fromPeriod;
    
    @Schema(description = "Период-цель")
    private Integer toPeriod;
}
