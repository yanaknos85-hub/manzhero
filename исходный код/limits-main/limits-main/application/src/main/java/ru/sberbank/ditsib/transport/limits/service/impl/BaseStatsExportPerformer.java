package ru.sberbank.ditsib.transport.limits.service.impl;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.LimitSharingStatsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;
import ru.sberbank.ditsib.transport.limits.service.StatsExportPerformer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for export statistics.
 */
abstract class BaseStatsExportPerformer implements StatsExportPerformer {

    private final Map<XSSFSheet, XSSFCellStyle> styles = new HashMap<>();

    @Override
    public XSSFSheet initSheet(XSSFWorkbook workbook, int maxLevel, LimitStatsDTO data) {
        var headers = new HashMap<Integer, String>();
        setUpLimitHeaders(headers);
        var headersSharing = new HashMap<Integer, String>();
        setUpLimitSharingHeaders(headersSharing);

        // header styling
        var headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        var headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        var sheet = workbook.createSheet(title(data));

        var rowIndex = 0;
        var headerRow = sheet.createRow(rowIndex);
        var headerColumnIndex = 0;

        sheet.setColumnWidth(headerColumnIndex, (15) * 256);

        var cell = headerRow.createCell(headerColumnIndex);
        cell.setCellValue("Год");
        cell.setCellStyle(headerStyle);
        headerColumnIndex++;

        for (var i = 0; i < maxLevel; i++) {
            sheet.setColumnWidth(headerColumnIndex, (15) * 256);
            cell = headerRow.createCell(headerColumnIndex);
            cell.setCellValue("Подразделение");
            cell.setCellStyle(headerStyle);
            headerColumnIndex++;
        }
        for (var i = 0; i < 11; i++) {
            sheet.setColumnWidth(headerColumnIndex, (15) * 256);
            cell = headerRow.createCell(headerColumnIndex);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            headerColumnIndex++;
        }

        headerColumnIndex = exportLimitSharingHeadersToXlsx(sheet, headerRow, headerColumnIndex,
            headersSharing, headerStyle, "TAXI");
        headerColumnIndex = exportLimitSharingHeadersToXlsx(sheet, headerRow, headerColumnIndex,
            headersSharing, headerStyle, "PRIVATE");
        exportLimitSharingHeadersToXlsx(sheet, headerRow, headerColumnIndex, headersSharing, headerStyle, "PUBLIC");
        styles.put(sheet, headerStyle);
        return sheet;
    }

    @Override
    public void addItem(XSSFSheet sheet, int maxLevel, LimitStatsDTO item) {
        var row = sheet.createRow(sheet.getLastRowNum() + 1);
        var columnIndex = 0;

        var cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getYear());

        for (var i = 0; i < maxLevel; i++) {
            var name = "";
            if (item.getDepartmentLevel() == i) {
                name = item.getDepartmentName();
                if (!StringUtils.hasText(name)) {
                    name = item.getDepartmentId().toString();
                }
            }
            cell = row.createCell(columnIndex++);
            cell.setCellValue(name);
        }

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getEmployeesNumber());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getBudgetYear().doubleValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getBudgetPeriod().doubleValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getSumSpentYear().doubleValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getSumSpentPeriod().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getSumReservedYear().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getSumBalanceYear().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getPerEmployeeSpent().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getCurrentDateEconomy().multiply(BigDecimal.valueOf(100)).longValue());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getProcentUsedYear());

        cell = row.createCell(columnIndex++);
        cell.setCellValue(item.getProcentUsedPeriod());

        var limitSharingStatsDTO = getForTransportType(item, TransportTypeEnum.TAXI);
        columnIndex =
            exportLimitSharingValuesToXlsx(row, columnIndex, limitSharingStatsDTO, styles.get(sheet));
        limitSharingStatsDTO = getForTransportType(item, TransportTypeEnum.PERSONAL);
        columnIndex =
            exportLimitSharingValuesToXlsx(row, columnIndex, limitSharingStatsDTO, styles.get(sheet));
        limitSharingStatsDTO = getForTransportType(item, TransportTypeEnum.PUBLIC);
        exportLimitSharingValuesToXlsx(row, columnIndex, limitSharingStatsDTO, styles.get(sheet));
    }

    @Override
    public void close(XSSFSheet sheet) {
        styles.remove(sheet);
    }

    /**
     * Add cells with values to a row.
     *
     * @param row the row to add cells.
     * @param item item with values.
     */
    protected abstract void addCells(Row row, LimitSharingStatsDTO item);

    /**
     * Get a title of a page.
     *
     * @param item item to get the title of the page.
     * @return the title of the page.
     */
    protected abstract String title(LimitStatsDTO item);

    private void setUpLimitHeaders(Map<Integer, String> headers) {
        headers.put(0, "Численность орг.стр, шт.ед. (ПШЕ)");
        headers.put(1, "Лимит средств на год, тыс.руб.");
        headers.put(2, "Лимит средств на месяц, тыс.руб. ");
        headers.put(3, "Фактические расходы на дату, на год, тыс.руб.");
        headers.put(4, "Фактические расходы на дату, на месяц, тыс.руб. ");
        headers.put(5, "Зарезервированы средства на дату, тыс.руб. ");
        headers.put(6, "Фактический остаток средств на дату, тыс.руб.");
        headers.put(7, "Удельный расход, руб./ численность шт.ед. ПШЕ");
        headers.put(8, "Возможная экономия");
        headers.put(9, "% использования лимитов на дату годовой");
        headers.put(10, "% использования лимитов на дату месячный ");
    }

    private void setUpLimitSharingHeaders(Map<Integer, String> headersSharing) {
        headersSharing.put(0, "Расходы на дату");
        headersSharing.put(1, "Фактический остаток");
        headersSharing.put(2, "Удельный расход, руб./шт.ед.");
        headersSharing.put(3, "Возможная экономия");
        headersSharing.put(4, "% от расходов");
        headersSharing.put(5, "% от использования");
    }

    private int exportLimitSharingValuesToXlsx(
        Row row, int columnIndex, LimitSharingStatsDTO item, XSSFCellStyle headerStyle
    ) {
        var cell = row.createCell(columnIndex++);
        cell.setCellValue("");
        cell.setCellStyle(headerStyle);
        if (item != null) {
            addCells(row, item);
        } else {
            cell = row.createCell(columnIndex++);
            cell.setCellValue(0);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(0);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(0);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(0);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(0);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(0);
        }

        return columnIndex;
    }

    private int exportLimitSharingHeadersToXlsx(
        Sheet sheet, Row headerRow, int headerColumnIndex,
        Map<Integer, String> headersSharing, XSSFCellStyle headerStyle,
        String title
    ) {
        sheet.setColumnWidth(headerColumnIndex, (15) * 256);
        var cell = headerRow.createCell(headerColumnIndex);
        cell.setCellValue(title);
        cell.setCellStyle(headerStyle);
        headerColumnIndex++;
        for (var i = 0; i < 6; i++) {
            sheet.setColumnWidth(headerColumnIndex, (15) * 256);
            cell = headerRow.createCell(headerColumnIndex);
            cell.setCellValue(headersSharing.get(i));
            cell.setCellStyle(headerStyle);
            headerColumnIndex++;
        }
        return headerColumnIndex;
    }

    private LimitSharingStatsDTO getForTransportType(LimitStatsDTO limitStatsDTO, TransportTypeEnum transportType) {
        for (var limitSharingStatsDTO : limitStatsDTO.getLimitSharingStatsDTOList()) {
            if (limitSharingStatsDTO.getTransportType().equals(transportType)) {
                return limitSharingStatsDTO;
            }
        }
        return null;
    }

}
