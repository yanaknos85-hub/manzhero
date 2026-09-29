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

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Распределение по видам транспорта",
        description = "Распределение по видам транспорта")
public class LimitSpendingDTO {
    
    /**
     * Лимит ID
     */
    @Schema(description = "Лимит ID")
    private UUID limitId;
    
    /**
     * Sum
     */
    @Schema(description = "Целевой сотрудник")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumReserved;
    
    /**
     * Sum
     */
    @Schema(description = "Целевой сотрудник")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumSpent;
    
}
