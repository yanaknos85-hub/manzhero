package ru.sber.transport.tariff_fleet.service.excel.impl;

import lombok.experimental.UtilityClass;
import ru.sber.transport.spreadsheet.excel.Excel;
import ru.sber.transport.tariff_fleet.exception.FileUploadException;
import ru.sber.transport.tariff_fleet.service.excel.ExcelExportProperties;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@UtilityClass
public class BaseExcelExporter {
    
    public static byte[] export(List<?> entries, Class<?> clazz, ExcelExportProperties properties) {
        var excel = new Excel(properties.fileType());
        var sheet = excel.addSheet("Лист1", clazz);
        sheet.getHeaderStyle().setWrapText(true);
        properties.fieldsInfo().forEach(column -> sheet.addColumn(column.excelFieldName(), column.javaFieldName()));
        sheet.setData(ReflectionUtils.cast(entries));
        
        try (var baos = new ByteArrayOutputStream()) {
            excel.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new FileUploadException();
        }
    }
}