package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;

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
@Schema(title = "Лимит", description = "Входные данные для создания лимита лимита")
public class EmpLimitDTO {
    /**
     * Year
     */
    @NotNull
    @Schema(description = "Год")
    private Integer year;
    
    /**
     * Limit sharing type
     */
    @NotNull
    @Schema(description = "Тип распределения")
    private LimitSharingType limitSharingType;
    
    /**
     * Limit service type
     */
    @NotNull
    @Schema(description = "Услуга")
    private String limitServiceType;
    
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
     * Parent limit
     */
    @Schema(description = "Родительский лимит")
    private UUID parentLimitId;
    
    /**
     * Target employee
     */
    @Schema(description = "Идентификатор сотрудника")
    private UUID targetEmployee;
}
