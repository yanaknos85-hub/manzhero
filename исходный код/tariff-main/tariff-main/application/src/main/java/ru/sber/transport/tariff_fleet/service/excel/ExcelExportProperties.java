package ru.sber.transport.tariff_fleet.service.excel;

import ru.sber.transport.spreadsheet.excel.WorkbookType;

import java.util.List;

public record ExcelExportProperties(
        WorkbookType fileType,
        List<ExcelFieldInfo> fieldsInfo
) {
}
