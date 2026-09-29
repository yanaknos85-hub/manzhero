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
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Экономия", description = "Выходные данные по экономии")
public class EconomyDTO {
    /**
     * Department.
     */
    @Schema(description = "Подразделение")
    private String department;
    
    /**
     * Year
     */
    @Schema(description = "Период")
    private Integer period;
    
    /**
     * Sum
     */
    @Schema(description = "Сумма экономии")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumEconomy;
    
    /**
     * Sum
     */
    @Schema(description = "Сумма за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumPerPeriod;
    
    /**
     * Limit transport type
     */
    @Schema(description = "Вид транспорта")
    private TransportTypeEnum transportType;
    
    /**
     * Sum
     */
    @Schema(description = "Процент")
    private Long percent;
}

    

    

