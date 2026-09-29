package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Остатки за период", description = "Остатки за период")
public class RemainsPerPeriodDTO {

    /**
     * Request
     */
    @Schema(description = "Идентификатор заявки")
    private UUID requestId;

    /**
     * Limit
     */
    @Schema(description = "Идентификатор лимита")
    private UUID limitId;

    /**
     * Limit Type
     */
    @Schema(description = "Тип лимита")
    private String limitType;

    /**
     * Limit
     */
    @Schema(description = "Человекочитаемый идентификатор лимита")
    private String limitHumanId;
    
    /**
     * Department.
     */
    @Schema(description = "Подразделение")
    private UUID departmentId;
    
    /**
     * Year
     */
    @Schema(description = "Период")
    private Integer period;
    
    /**
     * Sum
     */
    @Schema(description = "Остатки за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal balancePerPeriod;
}

    

    

