package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodSerializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Object with data of client.
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class LimitStatsV2DTO {
        private UUID departmentId;
        private String departmentName;
        private int departmentLevel;
        private int year;
        @JsonSerialize(using = PeriodSerializer.class)
        private Period period;
        private long employeesNumber;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal budgetYear;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal budgetPeriod;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal sumSpentYear;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal sumSpentPeriod;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal sumReservedYear;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal sumBalanceYear;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal perEmployeeBudget;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal perEmployeeSpent;
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal currentDateEconomy;
        private long percentUsedYear;
        private long percentUsedPeriod;
        private List<LimitSharingStatsV2DTO> limitSharingStatsDTOList;
}
