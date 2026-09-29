package ru.sberbank.ditsib.transport.limits.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class LimitDataFileDto {
    private String limitObjectType;
    @EqualsAndHashCode.Include
    private String limitType;
    @EqualsAndHashCode.Include
    private String departmentCode;
    private String limitObjectName;
    @EqualsAndHashCode.Include
    private int year;
    @EqualsAndHashCode.Include
    private String transportType;
    private BigDecimal sum;
    private BigDecimal balance;
    private String status;
}
