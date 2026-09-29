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
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;

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
@Schema(title = "Движение денежных средств по лимитами",
        description = "Движение денежных средств по лимитами")
public class LimitTransferHistoryDTO {
    
    /**
     * Year
     */
    @NotNull
    @Schema(description = "Год")
    private Integer year;
    
    /**
     * Sum
     */
    @Schema(description = "Сумма")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum;
    
    /**
     * Source limit id
     */
    @Schema(description = "Исходный лимит")
    private UUID sourceLimit;
    
    /**
     * Target limit id
     */
    @Schema(description = "Целевой лимит")
    private UUID targetLimit;
    
    /**
     * Source transport type
     */
    @Schema(description = "Исходный тип транспорта")
    private TransportTypeEnum sourceTransportType;
    
    /**
     * Target transport type
     */
    @Schema(description = "Целевой тип транспорта")
    private TransportTypeEnum targetTransportType;
    
    /**
     * Period
     */
    @Schema(description = "Исходный период")
    private Integer sourcePeriod;
    
    /**
     * Period
     */
    @Schema(description = "Целевой период")
    private Integer targetPeriod;
    
    /**
     * History type
     */
    @Schema(description = "Тип")
    private LimitTransferHistoryType historyType;
}
