package ru.sber.transport.trips_reports.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.web.controller.impl.ReportControllerImpl;
import ru.sber.transport.trips_reports.web.dto.ReportSearchDto;
import ru.sber.transport.trips_reports.web.dto.ReportsDto;
import ru.sber.transport.trips_reports.web.dto.TripStatus;
import ru.sber.transport.trips_reports.web.service.ReportService;
import ru.sber.transport.trips_reports.web.service.ResultProcessor;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ReportControllerImpl")
class ReportControllerImplTest {

    @Mock
    private ReportService reportService;

    @Mock
    private ResultProcessor resultProcessor;

    @InjectMocks
    private ReportControllerImpl tripController;

    @Test
    @DisplayName("Получение всех отчетов по ID контрагента")
    void testGetTrips() {
        UUID contractorId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        ReportSearchDto searchDto = new ReportSearchDto();
        List<TripStatus> statuses = List.of(TripStatus.ORDER_CANCELLED_BY_DRIVER);

        Reports reports = new Reports();
        reports.setHumanReadableId("HRID123");
        reports.setRequestHumanReadableId("REQ123");

        Iterable<Reports> reportsList = List.of(reports);

        when(reportService.getAll(contractorId, searchDto, statuses)).thenReturn(reportsList);

        ReportsDto reportsDto = new ReportsDto(reportId,
                "HRID123",
                "REQ123",
                "2023-10-01",
                List.of("Start Point 1"),
                List.of("Waypoint 1", "Waypoint 2"),
                List.of("End Point 1"),
                15,
                120.5,
                500.0,
                10.0,
                20.0,
                "COMPLETED",
                "No issues",
                "BUSINESS",
                "CONTRACT123",
                "John Doe",
                "AB123CD",
                List.of("Passenger 1", "Passenger 2"),
                "Jane Smith",
                "2023-10-01T10:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        when(resultProcessor.process(any(Iterable.class))).thenReturn(List.of(reportsDto));

        Iterable<ReportsDto> result = tripController.getTrips(contractorId, searchDto, statuses);

        assertNotNull(result);
        List<ReportsDto> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(1, resultList.size());
        assertEquals("HRID123", resultList.get(0).humanReadableId());
        assertEquals("REQ123", resultList.get(0).requestHumanReadableId());
        assertEquals("2023-10-01", resultList.get(0).desiredDate());
        assertEquals(List.of("Start Point 1"), resultList.get(0).routeStart());
        assertEquals(List.of("Waypoint 1", "Waypoint 2"), resultList.get(0).routeWaypoints());
        assertEquals(List.of("End Point 1"), resultList.get(0).routeEnd());
        assertEquals(15, resultList.get(0).expectedWaitTime());
        assertEquals(120.5, resultList.get(0).expectedDistance());
        assertEquals(500.0, resultList.get(0).minRideDistanceCost());
        assertEquals(10.0, resultList.get(0).waitCostPerMin());
        assertEquals(20.0, resultList.get(0).rideCostPerKm());
        assertEquals("COMPLETED", resultList.get(0).status());
        assertEquals("No issues", resultList.get(0).comment());
        assertEquals("BUSINESS", resultList.get(0).tripType());
        assertEquals("CONTRACT123", resultList.get(0).contractNumber());
        assertEquals("John Doe", resultList.get(0).driverFullName());
        assertEquals("AB123CD", resultList.get(0).vehicleNumber());
        assertEquals(List.of("Passenger 1", "Passenger 2"), resultList.get(0).passengerFullNames());
        assertEquals("Jane Smith", resultList.get(0).authorFullName());
        assertEquals("2023-10-01T10:00:00", resultList.get(0).creationTime());
        assertEquals(12L, resultList.get(0).expectedTime());
        assertEquals(13.13, resultList.get(0).factDistance());
        assertEquals(131313L, resultList.get(0).factTime());
        assertEquals(1313L, resultList.get(0).factWaitTime());
        assertEquals(10.01, resultList.get(0).factCost());

        verify(reportService).getAll(contractorId, searchDto, statuses);
        verify(resultProcessor).process(any(Iterable.class));
    }

    @Test
    @DisplayName("Получение детальной информации об отчете по ID")
    void testGetTrip() {
        UUID reportId = UUID.randomUUID();
        Reports report = new Reports();
        report.setHumanReadableId("HRID123");
        when(reportService.find(reportId)).thenReturn(report);

        ReportsDto reportsDto = new ReportsDto(reportId,
                "HRID123",
                "REQ123",
                "2023-10-01",
                List.of("Start Point 1"),
                List.of("Waypoint 1", "Waypoint 2"),
                List.of("End Point 1"),
                15,
                120.5,
                500.0,
                10.0,
                20.0,
                "COMPLETED",
                "No issues",
                "BUSINESS",
                "CONTRACT123",
                "John Doe",
                "AB123CD",
                List.of("Passenger 1", "Passenger 2"),
                "Jane Smith",
                "2023-10-01T10:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        when(resultProcessor.process(report)).thenReturn(reportsDto);

        ReportsDto result = tripController.getTrip(reportId);

        assertNotNull(result);
        assertEquals("HRID123", result.humanReadableId());
        assertEquals("REQ123", result.requestHumanReadableId());
        assertEquals("2023-10-01", result.desiredDate());

        verify(reportService).find(reportId);
        verify(resultProcessor).process(report);
    }
}