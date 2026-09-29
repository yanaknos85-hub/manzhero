package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.limits.dto.LimitSharingStatsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class StatsExportPerformerPeriodImpl extends BaseStatsExportPerformer {

    @Override
    public int order() {
        return 0;
    }

    @Override
    protected String title(LimitStatsDTO item) {
        return "Период %d".formatted(item.getPeriod() + 1);
    }

    @Override
    protected void addCells(Row row, LimitSharingStatsDTO item) {
        var columnIndex = row.getLastCellNum();

        var cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getSumSpentPeriod().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getBalancePeriod().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getPerEmployeeSpentPeriod().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getCurrentDateEconomyPeriod().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getProcentSpentPeriod());

        cell = row.createCell(columnIndex);
        cell.setCellValue(item.getProcentUsedPeriod());
    }
}
