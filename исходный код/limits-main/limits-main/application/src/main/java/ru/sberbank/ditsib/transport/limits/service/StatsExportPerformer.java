package ru.sberbank.ditsib.transport.limits.service;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;

/**
 * Interface for exporting statistics.
 */
public interface StatsExportPerformer {

    /**
     * Init statistic sheet.
     *
     * @param workbook workbook to init sheet.
     * @param maxLevel maximum level of limit.
     * @param data data.
     * @return excel sheet to fulfill.
     */
    XSSFSheet initSheet(XSSFWorkbook workbook, int maxLevel, LimitStatsDTO data);

    /**
     * Add an item to a sheet.
     *
     * @param sheet the sheet to add the item.
     * @param maxLevel max level of limits.
     * @param item the item to add to the sheet.
     */
    void addItem(XSSFSheet sheet, int maxLevel, LimitStatsDTO item);

    /**
     * Close a workbook.
     *
     * @param workbook the workbook to close.
     */
    void close(XSSFSheet workbook);

    /**
     * Get an order number of the bean.
     *
     * @return the order number.
     */
    int order();
}
