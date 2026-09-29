package ru.sberbank.ditsib.transport.dto.limits;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * Limit owner
     */
    @NotNull
    @Schema(description = "Автор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID author;
    
    /**
     * Date and time or request creation
     */
    @NotNull
    @Schema(description = "Время создания", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime creationTime;
    
    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Сумма в копейках", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sum;
    
    /**
     * Sum
     */
    @Min(0)
    @NotNull
    @Schema(description = "Баланс", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long balance;
    
    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Период", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer periodNumber;
    
    /**
     * Limit limit
     */
    @Schema(description = "Распределение лимита", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID limitSharing;
    
    
    /**
     * Сумма зарезервированная за период
     */
    @Schema(description = "Сумма зарезервированная за период")
    Long sumReservedForCurrentPeriod;
    
    /**
     * Сумма перераспределеней за период
     */
    @Schema(description = "Сумма перераспределеней за период")
    Long sumResharingsPeriod;
}
