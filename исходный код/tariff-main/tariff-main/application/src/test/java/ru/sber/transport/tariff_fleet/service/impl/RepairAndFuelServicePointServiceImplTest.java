package ru.sber.transport.tariff_fleet.service.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import ru.sber.transport.tariff_fleet.config.ServicePointsConfig;
import ru.sber.transport.tariff_fleet.dto.service_point.ValidatedServicePointDto;
import ru.sber.transport.tariff_fleet.exception.excel.MaxFileSizeException;
import ru.sber.transport.tariff_fleet.validation.ExcelValidationResult;
import ru.sber.transport.tariff_fleet.validation.ServicePointExcelDtoValidator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairAndFuelServicePointServiceImplTest {
    
    @InjectMocks
    private RepairAndFuelServicePointServiceImpl repairServicePointService;
    @Mock
    private ServicePointsConfig config;
    @Mock
    private ServicePointExcelDtoValidator validator;

    @Test
    @SneakyThrows
    void validateUploadServicePoints() {
        var multipartFile = new MockMultipartFile("file", "file.xlsx", null,
                new ClassPathResource("/excel/service_points.xlsx").getContentAsByteArray());
        doReturn(DataSize.ofBytes(1)).when(config).maxFileSize();
        assertThrows(MaxFileSizeException.class, () -> repairServicePointService.validateUploadServicePoints(multipartFile));

        doReturn(DataSize.ofMegabytes(1)).when(config).maxFileSize();
        var maxRowsCount = 5;
        doReturn(maxRowsCount).when(config).maxRowCount();
        var validatedServicePointDtos = Instancio.createList(ValidatedServicePointDto.class);
        doReturn(new ExcelValidationResult<>(false, validatedServicePointDtos))
                .when(validator).validateAndMap(anyList());

        var actual = repairServicePointService.validateUploadServicePoints(multipartFile);

        verify(config, times(3)).maxFileSize();
        verify(validator).validateAndMap(anyList());
        assertThat(actual.servicePoints()).isEqualTo(validatedServicePointDtos);
        assertThat(actual.file()).isNotEmpty();
    }
}
