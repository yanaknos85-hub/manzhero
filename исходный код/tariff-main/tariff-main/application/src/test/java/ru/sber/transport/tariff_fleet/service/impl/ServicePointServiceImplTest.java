package ru.sber.transport.tariff_fleet.service.impl;


import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.sber.transport.tariff_fleet.database.dao.ServicePointRepository;
import ru.sber.transport.tariff_fleet.database.model.ServicePoint;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.UnsupportedDocumentTypeException;
import ru.sber.transport.tariff_fleet.service.RepairAndFuelServicePointService;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.sber.transport.tariff_fleet.constant.DocumentType.EWB;
import static ru.sber.transport.tariff_fleet.constant.DocumentType.REPAIR_AND_MAINTENANCE;

@ExtendWith(MockitoExtension.class)
class ServicePointServiceImplTest {

    @InjectMocks
    private ServicePointServiceImpl servicePointService;
    @Mock
    private RepairAndFuelServicePointService repairAndFuelServicePointService;
    @Mock
    private ServicePointRepository repository;

    @Test
    void saveAll() {
        var servicePoints = new ArrayList<ServicePoint>();
        doReturn(List.of()).when(repository).saveAll(servicePoints);
        servicePointService.saveAll(servicePoints);
        verify(repository).saveAll(servicePoints);
    }

    @Test
    void validateUploadServicePoints() {
        var multipartFile = new MockMultipartFile("file", new byte[0]);
        servicePointService.validateUploadServicePoints(REPAIR_AND_MAINTENANCE, multipartFile);
        verify(repairAndFuelServicePointService).validateUploadServicePoints(multipartFile);

        assertThrows(UnsupportedDocumentTypeException.class, () -> servicePointService.validateUploadServicePoints(EWB, multipartFile));
    }

    @Test
    void downloadServicePointsTemplate() {
        doReturn(new byte[1]).when(repairAndFuelServicePointService).downloadServicePointsTemplate();
        var actualBytes = servicePointService.downloadServicePointsTemplate();
        verify(repairAndFuelServicePointService).downloadServicePointsTemplate();
        assertThat(actualBytes).isNotEmpty();
    }

    @Test
    void shouldDropExistingPointsAndSaveNewOnes() {
        // Подготовка входных данных
        UUID contractId = UUID.randomUUID();
        List<ServicePointDto> pointsToCreate = List.of(
                new ServicePointDto("Москва, ул. Тверская, д. 1",
                        new BigDecimal("55.7558"), new BigDecimal("37.6173")),
                new ServicePointDto("Санкт-Петербург, Невский проспект, д. 1",
                        new BigDecimal("59.9386"), new BigDecimal("30.3141"))
        );

        doNothing().when(repository).deleteAllByContractId(any());

        given(repository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        var result = servicePointService.dropExistedPointsForContractAndCreate(contractId, pointsToCreate);

        assertThat(result).hasSize(pointsToCreate.size());

        // Проверяем корректность каждого элемента
        for (var i = 0; i < result.size(); i++) {
            var point = result.get(i);
            var model = pointsToCreate.get(i);

            assertThat(point.getAddress()).isEqualTo(model.address());
            assertThat(point.getLatitude()).isEqualTo(model.latitude());
            assertThat(point.getLongitude()).isEqualTo(model.longitude());
            assertThat(point.getContractId()).isEqualTo(contractId);
        }
    }

    @Test
    void shouldHandleEmptyInputGracefully() {
        var contractId = UUID.randomUUID();
        var emptyList = new ArrayList<ServicePointDto>();
        var result = servicePointService.dropExistedPointsForContractAndCreate(contractId, emptyList);
        assertThat(result).isEmpty();
    }

    @Test
    void testBuildServicePointsFileWithValidEntries() throws Exception {
        var points = List.of(
                new ServicePoint(UUID.randomUUID(), "Москва, ул. Тверская, д. 1", BigDecimal.valueOf(55.7558), BigDecimal.valueOf(37.6173),
                        UUID.randomUUID(), true),
                new ServicePoint(UUID.randomUUID(), "Санкт-Петербург, Невский проспект, д. 1", BigDecimal.valueOf(59.9386),
                        BigDecimal.valueOf(30.3141), UUID.randomUUID(), true)
        );

        var resultBytes = servicePointService.buildServicePointsFile(points);

        assertThat(resultBytes).isNotNull();

        var workbook = WorkbookFactory.create(new ByteArrayInputStream(resultBytes));
        var sheet = workbook.getSheetAt(0);
        var headerRow = sheet.getRow(0);
        assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Номер по порядку");
        assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Адрес точки обслуживания");
        assertThat(headerRow.getCell(2).getStringCellValue()).isEqualTo("Широта");
        assertThat(headerRow.getCell(3).getStringCellValue()).isEqualTo("Долгота");

        var firstDataRow = sheet.getRow(1);
        assertThat(firstDataRow.getCell(0).getNumericCellValue()).isEqualTo(1);
        assertThat(firstDataRow.getCell(1).getStringCellValue()).isEqualTo("Москва, ул. Тверская, д. 1");
        assertThat(firstDataRow.getCell(2).getNumericCellValue()).isEqualTo(55.7558d);
        assertThat(firstDataRow.getCell(3).getNumericCellValue()).isEqualTo(37.6173d);
    }

    @Test
    @DisplayName("Пустой файл успешно создается")
    void testBuildServicePointsFileWithEmptyList() {
        List<ServicePoint> emptyPoints = List.of();
        byte[] resultBytes = servicePointService.buildServicePointsFile(emptyPoints);
        assertThat(resultBytes).isNotNull();
    }
}
