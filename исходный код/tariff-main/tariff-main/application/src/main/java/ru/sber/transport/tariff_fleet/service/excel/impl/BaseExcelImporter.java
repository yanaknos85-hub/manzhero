package ru.sber.transport.tariff_fleet.service.excel.impl;


import lombok.experimental.UtilityClass;
import ru.sber.transport.spreadsheet.excel.Excel;
import ru.sber.transport.spreadsheet.excel.WorkbookType;
import ru.sber.transport.tariff_fleet.exception.FileNoNameException;
import ru.sber.transport.tariff_fleet.exception.FileUnsupportedExtensionException;
import ru.sber.transport.tariff_fleet.exception.excel.TooManyRowsException;
import ru.sber.transport.tariff_fleet.service.excel.ExcelImportProperties;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@UtilityClass
public class BaseExcelImporter {
    
    public static List<Object> parse(InputStream inputStream, Class<?> clazz, ExcelImportProperties importProperties) throws IOException {
        var excel = new Excel(importProperties.fileType());
        excel.load(inputStream);
        var sheet = excel.addSheet(0, clazz);
        for (var fieldInfo : importProperties.fieldsInfo()) {
            sheet.addColumn(fieldInfo.excelFieldName(), fieldInfo.javaFieldName());
        }
        
        var rowNumber = 0;
        var result = new ArrayList<>();
        var dataIterator = excel.getDataIterator(ReflectionUtils.cast(sheet), null, false, true);
        while (dataIterator.hasNext()) {
            rowNumber++;
            if (rowNumber > importProperties.maxRowsCount()) {
                throw new TooManyRowsException(importProperties.maxRowsCount());
            }
            result.add(ReflectionUtils.cast(dataIterator.next()));
        }
        return result;
    }
    
    public static WorkbookType resolveFileType(String fileName) {
        if (!hasText(fileName)) {
            throw new FileNoNameException();
        }
        
        var split = fileName.split("\\.");
        var extension = split[split.length - 1];
        if (extension.equals("xlsx")) {
            return WorkbookType.XLSX;
        } else if (extension.equals("xls")) {
            return WorkbookType.XLS;
        } else {
            throw new FileUnsupportedExtensionException("xlsx, xls");
        }
    }
    
}
