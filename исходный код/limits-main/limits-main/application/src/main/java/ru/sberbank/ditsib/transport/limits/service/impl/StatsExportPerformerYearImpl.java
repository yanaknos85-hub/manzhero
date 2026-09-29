package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.limits.dto.LimitSharingStatsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class StatsExportPerformerYearImpl extends BaseStatsExportPerformer {

    @Override
    public int order() {
        return 1;
    }

    @Override
    protected String title(LimitStatsDTO item) {
        return "Год %d".formatted(item.getYear());
    }

    @Override
    protected void addCells(Row row, LimitSharingStatsDTO item) {
        var columnIndex = row.getLastCellNum();

        var cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getSumSpent().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getBalance().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getPerEmployeeSpent().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getCurrentDateEconomy().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getProcentSpent());

        cell = row.createCell(columnIndex);
        cell.setCellValue(item.getProcentUsed());
    }

}
