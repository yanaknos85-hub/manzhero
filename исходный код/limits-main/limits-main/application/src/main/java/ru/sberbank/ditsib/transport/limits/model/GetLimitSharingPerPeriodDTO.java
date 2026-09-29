package ru.sberbank.ditsib.transport.limits.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
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
@Schema(title = "Распределение по видам транспорта (чтение)",
        description = "Распределение по видам транспорта для чтения")
public class GetLimitSharingPerPeriodDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Limit owner
     */
    @NotNull
    @Schema(description = "Автор")
    private UUID author;
    
    /**
     * Date and time or request creation
     */
    @NotNull
    @Schema(description = "Время создания")
    private LocalDateTime creationTime;
    
    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Сумма в копейках")
    @Builder.Default
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sum = BigDecimal.ZERO;
    
    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Баланс")
    @Builder.Default
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal balance = BigDecimal.ZERO;
    
    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Период")
    private Integer periodNumber;
    
    /**
     * Limit limit
     */
    @Schema(description = "Распределение лимита")
    private UUID limitSharing;
    
    
    /**
     * Сумма зарезервированная за период
     */
    @Schema(description = "Сумма зарезервированная за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumReservedForCurrentPeriod;
    
    /**
     * Сумма перераспределеней за период
     */
    @Schema(description = "Сумма перераспределеней за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumResharingsPeriod;
}

