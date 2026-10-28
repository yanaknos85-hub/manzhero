package ru.sber.transport.trips_reports.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.sber.transport.trips_reports.messaging.mapper.ReportMapper;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.web.dto.ReportsDto;
import ru.sber.transport.trips_reports.web.service.ResultProcessor;
import ru.sber.transport.trips_reports.web.service.impl.ResultProcessorImpl;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ResultProcessorImpl")
class ResultProcessorImplTest {

    @Mock
    private ReportMapper mapper;

    private ResultProcessor resultProcessor;

    @BeforeEach
    void setUp() {
        resultProcessor = new ResultProcessorImpl(mapper);
    }

    @Test
    @DisplayName("Обработка списка отчетов")
    void testProcessIterable() {
        UUID reportId = UUID.randomUUID();
        Reports report1 = new Reports();
        report1.setHumanReadableId("HRID123");

        Reports report2 = new Reports();
        report2.setHumanReadableId("HRID124");

        Iterable<Reports> reportsList = List.of(report1, report2);

        ReportsDto reportDto1 = new ReportsDto(reportId,
                "HRID123",
                "REQ123",
                "2023-10-01",
                List.of("Start Point A"),
                List.of("Waypoint A1", "Waypoint A2"),
                List.of("End Point A"),
                10,
                100.0,
                400.0,
                8.0,
                18.0,
                "IN_PROGRESS",
                "No comments",
                "PERSONAL",
                "CONTRACTA",
                "Driver A",
                "AA111AA",
                List.of("Passenger A1"),
                "Author A",
                "2023-10-01T09:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        ReportsDto reportDto2 = new ReportsDto(reportId,
                "HRID124",
                "REQ124",
                "2023-10-02",
                List.of("Start Point B"),
                List.of("Waypoint B1"),
                List.of("End Point B"),
                20,
                150.0,
                600.0,
                12.0,
                22.0,
                "COMPLETED",
                "Delayed",
                "BUSINESS",
                "CONTRACTB",
                "Driver B",
                "BB222BB",
                List.of("Passenger B1", "Passenger B2"),
                "Author B",
                "2023-10-02T11:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        when(mapper.toDto(report1)).thenReturn(reportDto1);
        when(mapper.toDto(report2)).thenReturn(reportDto2);

        Iterable<ReportsDto> result = resultProcessor.process(reportsList);

        assertNotNull(result);
        List<ReportsDto> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());

        ReportsDto dto1 = resultList.get(0);
        assertEquals("HRID123", dto1.humanReadableId());
        assertEquals("REQ123", dto1.requestHumanReadableId());
        assertEquals("2023-10-01", dto1.desiredDate());

        ReportsDto dto2 = resultList.get(1);
        assertEquals("HRID124", dto2.humanReadableId());
        assertEquals("REQ124", dto2.requestHumanReadableId());
        assertEquals("2023-10-02", dto2.desiredDate());

        verify(mapper).toDto(report1);
        verify(mapper).toDto(report2);
    }

    @Test
    @DisplayName("Обработка одного отчета")
    void testProcessSingleReport() {
        UUID reportId = UUID.randomUUID();
        Reports report = new Reports();
        report.setHumanReadableId("ОТЧЕТ125");
        report.setRequestHumanReadableId("ЗАПРОС125");
        report.setDesiredDate("03.10.2023");
        report.setRouteStart(List.of("Начало маршрута C"));
        report.setRouteWaypoints(List.of("Промежуточная точка C1"));
        report.setRouteEnd(List.of("Конец маршрута C"));
        report.setExpectedWaitTime(25);
        report.setExpectedDistance(200.0);
        report.setMinRideDistanceCost(700.0);
        report.setWaitCostPerMin(10.0);
        report.setRideCostPerKm(25.0);
        report.setStatus("В ПРОЦЕССЕ");
        report.setComment("Новый комментарий");
        report.setTripType("ЛИЧНЫЙ");
        report.setContractNumber("ДОГОВОРC");
        report.setDriverFullName("Сергей Сергеев");
        report.setVehicleNumber("CC333CC");
        report.setPassengerFullNames(List.of("Пассажир C1"));
        report.setAuthorFullName("Автор C");
        report.setCreationTime("03.10.2023 13:00:00");
        report.setFactDistance(13.13);
        report.setFactTime(131313L);
        report.setFactWaitTime(1313L);
        report.setFactCost(10.01);

        ReportsDto reportDto = new ReportsDto(reportId,
                "ОТЧЕТ125",
                "ЗАПРОС125",
                "03.10.2023",
                List.of("Начало маршрута C"),
                List.of("Промежуточная точка C1"),
                List.of("Конец маршрута C"),
                25,
                200.0,
                700.0,
                10.0,
                25.0,
                "В ПРОЦЕССЕ",
                "Новый комментарий",
                "ЛИЧНЫЙ",
                "ДОГОВОРC",
                "Сергей Сергеев",
                "CC333CC",
                List.of("Пассажир C1"),
                "Автор C",
                "03.10.2023 13:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        when(mapper.toDto(report)).thenReturn(reportDto);

        ReportsDto result = resultProcessor.process(report);

        assertNotNull(result);
        assertEquals("ОТЧЕТ125", result.humanReadableId());
        assertEquals("ЗАПРОС125", result.requestHumanReadableId());
        assertEquals("03.10.2023", result.desiredDate());
        assertEquals(List.of("Начало маршрута C"), result.routeStart());
        assertEquals(List.of("Промежуточная точка C1"), result.routeWaypoints());
        assertEquals(List.of("Конец маршрута C"), result.routeEnd());
        assertEquals(25, result.expectedWaitTime());
        assertEquals(200.0, result.expectedDistance());
        assertEquals(700.0, result.minRideDistanceCost());
        assertEquals(10.0, result.waitCostPerMin());
        assertEquals(25.0, result.rideCostPerKm());
        assertEquals("В ПРОЦЕССЕ", result.status());
        assertEquals("Новый комментарий", result.comment());
        assertEquals("ЛИЧНЫЙ", result.tripType());
        assertEquals("ДОГОВОРC", result.contractNumber());
        assertEquals("Сергей Сергеев", result.driverFullName());
        assertEquals("CC333CC", result.vehicleNumber());
        assertEquals(List.of("Пассажир C1"), result.passengerFullNames());
        assertEquals("Автор C", result.authorFullName());
        assertEquals("03.10.2023 13:00:00", result.creationTime());
        assertEquals(12L, result.expectedTime());
        assertEquals(13.13, result.factDistance());
        assertEquals(131313L, result.factTime());
        assertEquals(1313L, result.factWaitTime());
        assertEquals(10.01, result.factCost());

        verify(mapper).toDto(report);
    }

    @Test
    @DisplayName("Обработка Page<Reports>")
    void testProcessPage() {
        UUID reportId = UUID.randomUUID();
        Reports report1 = new Reports();
        report1.setHumanReadableId("HRID123");

        Reports report2 = new Reports();
        report2.setHumanReadableId("HRID124");

        ReportsDto reportDto1 = new ReportsDto(reportId,
                "HRID123",
                "REQ123",
                "2023-10-01",
                List.of("Start Point A"),
                List.of("Waypoint A1"),
                List.of("End Point A"),
                10,
                100.0,
                400.0,
                8.0,
                18.0,
                "IN_PROGRESS",
                "No comments",
                "PERSONAL",
                "CONTRACTA",
                "Driver A",
                "AA111AA",
                List.of("Passenger A1"),
                "Author A",
                "2023-10-01T09:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        ReportsDto reportDto2 = new ReportsDto(reportId,
                "HRID124",
                "REQ124",
                "2023-10-02",
                List.of("Start Point B"),
                List.of("Waypoint B1"),
                List.of("End Point B"),
                20,
                150.0,
                600.0,
                12.0,
                22.0,
                "COMPLETED",
                "Delayed",
                "BUSINESS",
                "CONTRACTB",
                "Driver B",
                "BB222BB",
                List.of("Passenger B1", "Passenger B2"),
                "Author B",
                "2023-10-02T11:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        Page<Reports> reportsPage = new PageImpl<>(List.of(report1, report2));

        when(mapper.toDto(report1)).thenReturn(reportDto1);
        when(mapper.toDto(report2)).thenReturn(reportDto2);

        Iterable<ReportsDto> result = resultProcessor.process(reportsPage);

        assertNotNull(result);
        List<ReportsDto> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());

        ReportsDto dto1 = resultList.get(0);
        assertEquals("HRID123", dto1.humanReadableId());
        ReportsDto dto2 = resultList.get(1);
        assertEquals("HRID124", dto2.humanReadableId());

        verify(mapper).toDto(report1);
        verify(mapper).toDto(report2);
    }

    @Test
    @DisplayName("Обработка Collection<Reports>")
    void testProcessCollection() {
        UUID reportId = UUID.randomUUID();
        Reports report1 = new Reports();
        report1.setHumanReadableId("HRID123");

        Reports report2 = new Reports();
        report2.setHumanReadableId("HRID124");

        ReportsDto reportDto1 = new ReportsDto(reportId,
                "HRID123",
                "REQ123",
                "2023-10-01",
                List.of("Start Point A"),
                List.of("Waypoint A1"),
                List.of("End Point A"),
                10,
                100.0,
                400.0,
                8.0,
                18.0,
                "IN_PROGRESS",
                "No comments",
                "PERSONAL",
                "CONTRACTA",
                "Driver A",
                "AA111AA",
                List.of("Passenger A1"),
                "Author A",
                "2023-10-01T09:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        ReportsDto reportDto2 = new ReportsDto(reportId,
                "HRID124",
                "REQ124",
                "2023-10-02",
                List.of("Start Point B"),
                List.of("Waypoint B1"),
                List.of("End Point B"),
                20,
                150.0,
                600.0,
                12.0,
                22.0,
                "COMPLETED",
                "Delayed",
                "BUSINESS",
                "CONTRACTB",
                "Driver B",
                "BB222BB",
                List.of("Passenger B1", "Passenger B2"),
                "Author B",
                "2023-10-02T11:00:00",
                12L,
                13.13,
                131313L,
                1313L,
                10.01
        );

        Collection<Reports> reportsCollection = List.of(report1, report2);

        when(mapper.toDto(report1)).thenReturn(reportDto1);
        when(mapper.toDto(report2)).thenReturn(reportDto2);

        Iterable<ReportsDto> result = resultProcessor.process(reportsCollection);

        assertNotNull(result);
        List<ReportsDto> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());

        ReportsDto dto1 = resultList.get(0);
        assertEquals("HRID123", dto1.humanReadableId());
        ReportsDto dto2 = resultList.get(1);
        assertEquals("HRID124", dto2.humanReadableId());

        verify(mapper).toDto(report1);
        verify(mapper).toDto(report2);
    }

}