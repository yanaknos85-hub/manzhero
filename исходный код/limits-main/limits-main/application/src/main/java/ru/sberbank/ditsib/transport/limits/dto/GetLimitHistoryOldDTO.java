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
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryTypeOld;

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class GetLimitHistoryOldDTO {
    
    /**
     * Author of sharing
     */
    @Schema(description = "Автор")
    private UUID author;
    
    /**
     * Date and time or record creation.
     */
    @Schema(description = "Время создания")
    private LocalDateTime creationTime;
    
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
    private UUID limitId;
    
    /**
     * Source transport type
     */
    @Schema(description = "Исходный тип транспорта")
    private TransportTypeEnum transportType;
    
    /**
     * Year
     */
    @NotNull
    @Schema(description = "Год")
    private Integer year;
    
    /**
     * Period
     */
    @Schema(description = "Период")
    private Integer period;
    
    /**
     * History type
     */
    @Schema(description = "Тип")
    private LimitHistoryTypeOld historyType;
}
