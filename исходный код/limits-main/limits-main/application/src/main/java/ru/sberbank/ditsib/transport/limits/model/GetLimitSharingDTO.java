package ru.sberbank.ditsib.transport.limits.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.constraints.Min;
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
@Schema(title = "Распределение по видам транспорта (чтение)",
        description = "Распределение по видам транспорта для чтения")
public class GetLimitSharingDTO {
    
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
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Вид транспорта")
    private TransportTypeEnum transportType;
    
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
     * Limit id
     */
    @Schema(description = "Лимит")
    private UUID limitId;
    
    /**
     * Распределение лимита за текущий месяц
     */
    @Schema(description = "Распределение лимита за текущий период")
    GetLimitSharingPerPeriodDTO limitSharingPerPeriodDTO;
    
    /**
     * Сумма перераспределеней за год
     */
    @Schema(description = "Сумма перераспределеней за год")
    @Builder.Default
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumResharingsYear = BigDecimal.ZERO;
}

