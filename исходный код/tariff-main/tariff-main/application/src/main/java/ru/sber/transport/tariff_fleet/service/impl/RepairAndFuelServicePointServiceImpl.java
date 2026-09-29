package ru.sber.transport.tariff_fleet.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.spreadsheet.base.reader.exception.HeaderValidationException;
import ru.sber.transport.spreadsheet.excel.WorkbookType;
import ru.sber.transport.tariff_fleet.config.ServicePointsConfig;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointExcelDto;
import ru.sber.transport.tariff_fleet.dto.service_point.UploadServicePointsDto;
import ru.sber.transport.tariff_fleet.dto.service_point.ValidatedServicePointDto;
import ru.sber.transport.tariff_fleet.exception.FileUploadException;
import ru.sber.transport.tariff_fleet.exception.excel.ExcelHeaderValidationException;
import ru.sber.transport.tariff_fleet.exception.excel.MaxFileSizeException;
import ru.sber.transport.tariff_fleet.service.RepairAndFuelServicePointService;
import ru.sber.transport.tariff_fleet.service.excel.ExcelExportProperties;
import ru.sber.transport.tariff_fleet.service.excel.ExcelImportProperties;
import ru.sber.transport.tariff_fleet.service.excel.impl.BaseExcelExporter;
import ru.sber.transport.tariff_fleet.service.excel.impl.BaseExcelImporter;
import ru.sber.transport.tariff_fleet.validation.ServicePointExcelDtoValidator;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairAndFuelServicePointServiceImpl implements RepairAndFuelServicePointService {
    
    private final ServicePointExcelDtoValidator validator;
    private final ServicePointsConfig config;
    
    @Override
    public UploadServicePointsDto validateUploadServicePoints(MultipartFile file) {
        if (file.getSize() > config.maxFileSize().toBytes()) {
            throw new MaxFileSizeException(config.maxFileSize());
        }
        
        var fileType = BaseExcelImporter.resolveFileType(file.getOriginalFilename());
        try {
            var servicePointDtos = BaseExcelImporter.parse(file.getInputStream(), ServicePointExcelDto.class,
                                                           new ExcelImportProperties(fileType,
                                                                                     ServicePointExcelDto.getExcelFieldsInfo(),
                                                                                     config.maxRowCount()));
            var validationResult = validator.validateAndMap(ReflectionUtils.cast(servicePointDtos));
            
            var xlsxFile = validationResult.hasAnyError()
                           ? BaseExcelExporter.export(validationResult.enrtyList(),
                                                      ValidatedServicePointDto.class,
                                                      new ExcelExportProperties(fileType, ValidatedServicePointDto.getExcelFieldsInfo()))
                           : file.getBytes();
            return new UploadServicePointsDto(validationResult.enrtyList(), xlsxFile);
        } catch (HeaderValidationException e) {
            throw new ExcelHeaderValidationException();
        } catch (IOException e) {
            throw new FileUploadException();
        }
    }
    
    @Override
    public byte[] downloadServicePointsTemplate() {
        return BaseExcelExporter.export(List.of(), ServicePointExcelDto.class,
                                        new ExcelExportProperties(WorkbookType.XLSX, ServicePointExcelDto.getExcelFieldsInfo()));
    }

}
