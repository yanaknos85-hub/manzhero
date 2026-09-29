package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Object with data of client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitStatsDTO {
    
    /**
     * inserted items
     */
    @Schema(description = "ID подразделения")
    private UUID departmentId;
    
    /**
     * inserted items
     */
    @Schema(description = "Название подразделения")
    private String departmentName;
    
    /**
     * Бюджет
     */
    @Schema(description = "Уровень подразделения")
    private int departmentLevel = 0;
    
    /**
     * Бюджет
     */
    @Schema(description = "Год")
    private int year = 0;
    
    /**
     * Бюджет
     */
    @Schema(description = "Период")
    private int period = 0;
    
    /**
     * численность
     */
    @Schema(description = "Численность подразделения")
    private long employeesNumber = 0L;
    
    /**
     * Бюджет
     */
    @Schema(description = "Бюджет за год")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal budgetYear = BigDecimal.ZERO;
    
    /**
     * Бюджет
     */
    @Schema(description = "Бюджет за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal budgetPeriod = BigDecimal.ZERO;
    
    /**
     * Бюджет
     */
    @Schema(description = "Сумма потраченная за год")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumSpentYear = BigDecimal.ZERO;
    
    /**
     * Бюджет
     */
    @Schema(description = "Сумма потрачанная за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumSpentPeriod = BigDecimal.ZERO;
    
    /**
     * Бюджет
     */
    @Schema(description = "Сумма зарезервированная за период")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumReservedYear = BigDecimal.ZERO;
    
    /**
     * Бюджет
     */
    @Schema(description = "Баланс за год")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumBalanceYear = BigDecimal.ZERO;

    /**
     * Бюджет/численность
     */
    @Schema(description = "Бюджет/численность")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal perEmployeeBudget = BigDecimal.ZERO;
    
    /**
     * Бюджет/численность
     */
    @Schema(description = "Расход/численность")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal perEmployeeSpent = BigDecimal.ZERO;
    
    /**
     * экономия на дату
     */
    @Schema(description = "Экономия на дату")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal currentDateEconomy = BigDecimal.ZERO;
    
    /**
     * inserted items
     */
    @Schema(description = "% Использования лимитов за год")
    private long procentUsedYear = 0L;
    
    /**
     * inserted items
     */
    @Schema(description = "% Использования лимитов за период")
    private long procentUsedPeriod = 0L;

    /**
     * Total bad items
     */
    @Schema(description = "Данные по видам транспорта")
    List<LimitSharingStatsDTO> limitSharingStatsDTOList;

}
