package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;

/**
 * Object with data of client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitSharingStatsDTO {
    
    /**
     * transportType
     */
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportType;
    
    /**
     * total processed
     */
    @Schema(description = "выделенный лимит")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal budgetPerYear = BigDecimal.ZERO;
    
    /**
     * total processed
     */
    @Schema(description = "зарезервированный лимит")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumReserved = BigDecimal.ZERO;
    
    /**
     * total processed
     */
    @Schema(description = "фактические расходы на дату")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumSpent = BigDecimal.ZERO;
    
    /**
     * total processed
     */
    @Schema(description = "фактический остаток на дату")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal balance = BigDecimal.ZERO;
    
    /**
     * Бюджет/численность
     */
    @Schema(description = "Удельный бюджет на чел")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal perEmployeeBudget = BigDecimal.ZERO;

    /**
     * Бюджет/численность
     */
    @Schema(description = "Удельный расход на чел")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal perEmployeeSpent = BigDecimal.ZERO;

    /**
     * Бюджет/численность
     */
    @Schema(description = "Возможная экономия")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal currentDateEconomy = BigDecimal.ZERO;

    /**
     * Бюджет/численность
     */
    @Schema(description = "% расходов на данный вид транспорта ко всем расходам")
    private long procentSpent = 0L;

    /**
     * Бюджет/численность
     */
    @Schema(description = "% использования лимита")
    private long procentUsed = 0L;

    //---------------------------------------------------------

    /**
     * total processed
     */
    @Schema(description = "фактические расходы на дату")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal sumSpentPeriod = BigDecimal.ZERO;

    /**
     * total processed
     */
    @Schema(description = "фактический остаток на дату")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal balancePeriod = BigDecimal.ZERO;

    /**
     * Бюджет/численность
     */
    @Schema(description = "Удельный расход на чел")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal perEmployeeSpentPeriod = BigDecimal.ZERO;

    /**
     * Бюджет/численность
     */
    @Schema(description = "Возможная экономия")
    @JsonSerialize(using = SumSerializer.class)
    @JsonDeserialize(using = SumDeserializer.class)
    private BigDecimal currentDateEconomyPeriod = BigDecimal.ZERO;

    /**
     * Бюджет/численность
     */
    @Schema(description = "% расходов на данный вид транспорта ко всем расходам")
    private long procentSpentPeriod = 0L;

    /**
     * Бюджет/численность
     */
    @Schema(description = "% использования лимита")
    private long procentUsedPeriod = 0L;
}
