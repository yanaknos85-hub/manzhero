package ru.sber.transport.tariff_fleet.service.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.sber.transport.spreadsheet.excel.WorkbookType;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointExcelDto;
import ru.sber.transport.tariff_fleet.exception.FileNoNameException;
import ru.sber.transport.tariff_fleet.exception.FileUnsupportedExtensionException;
import ru.sber.transport.tariff_fleet.exception.excel.TooManyRowsException;
import ru.sber.transport.tariff_fleet.service.excel.ExcelImportProperties;
import ru.sber.transport.tariff_fleet.service.excel.impl.BaseExcelImporter;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseExcelImporterTest {
    
    
    @Test
    @SneakyThrows
    void parse() {
        try (var inputStream = Files.newInputStream(new ClassPathResource("/excel/service_points.xlsx").getFile().toPath(),
                                                    StandardOpenOption.READ)) {
            var importProperties = new ExcelImportProperties(WorkbookType.XLSX,
                                                             ServicePointExcelDto.getExcelFieldsInfo(),
                                                             1);
            assertThrows(TooManyRowsException.class, () -> BaseExcelImporter.parse(inputStream, ServicePointExcelDto.class, importProperties));
        }
    }
    
    @Test
    void resolveFileType() {
        assertThrows(FileNoNameException.class, () -> BaseExcelImporter.resolveFileType(""));
        assertThrows(FileUnsupportedExtensionException.class, () -> BaseExcelImporter.resolveFileType("file.txt"));
        assertEquals(WorkbookType.XLSX, BaseExcelImporter.resolveFileType("file.xlsx"));
        assertEquals(WorkbookType.XLS, BaseExcelImporter.resolveFileType("file.xls"));
    }
}
