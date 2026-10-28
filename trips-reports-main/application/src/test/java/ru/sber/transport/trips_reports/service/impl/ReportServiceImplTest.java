package ru.sber.transport.trips_reports.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.providers.trips.TripProvider;
import ru.sber.transport.trips_reports.web.dto.ReportSearchDto;
import ru.sber.transport.trips_reports.web.dto.TripStatus;
import ru.sber.transport.trips_reports.web.service.ReportService;
import ru.sber.transport.trips_reports.web.service.impl.ReportServiceImpl;

import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("trip_service_check")
@DisplayName("Проверка сервиса по работе с отчетами поездок")
class ReportServiceImplTest {

    private final TripProvider tripProvider = mock(TripProvider.class);
    private final ReportService reportService = new ReportServiceImpl(tripProvider);

    @Test
    @DisplayName("Получение всех поездок")
    void testGetAll() {
        UUID contractorId = UUID.randomUUID();
        ReportSearchDto searchData = new ReportSearchDto();
        List<TripStatus> statuses = Arrays.asList(TripStatus.ORDER_CANCELLED_BY_DRIVER, TripStatus.ORDER_CANCELLED_BY_CLIENT);

        List<Reports> tripsDtoList = new ArrayList<>();
        Reports tripsDto = new Reports();
        tripsDto.setHumanReadableId("HRID123");
        tripsDto.setRequestHumanReadableId("REQ123");
        tripsDto.setDesiredDate("01.01.2023");
        tripsDto.setRouteStart(List.of("Start Address"));
        tripsDto.setRouteWaypoints(List.of("Waypoint1", "Waypoint2"));
        tripsDto.setRouteEnd(List.of("End Address"));
        tripsDto.setExpectedWaitTime(15);
        tripsDto.setExpectedDistance(10.5);
        tripsDto.setMinRideDistanceCost(100.0);
        tripsDto.setWaitCostPerMin(5.0);
        tripsDto.setRideCostPerKm(10.0);
        tripsDto.setStatus("NEW");
        tripsDto.setComment("No comments");
        tripsDto.setTripType("Business");
        tripsDto.setContractNumber("CONTRACT123");
        tripsDto.setDriverFullName("Driver Name");
        tripsDto.setVehicleNumber("A123BC");
        tripsDto.setPassengerFullNames(List.of("Passenger One", "Passenger Two"));
        tripsDto.setAuthorFullName("Author Name");
        tripsDto.setCreationTime("01.01.2023 12:00:00");
        tripsDtoList.add(tripsDto);

        when(tripProvider.findAll(eq(contractorId), eq(searchData), eq(statuses)))
                .thenReturn(tripsDtoList);

        Iterable<Reports> result = reportService.getAll(contractorId, searchData, statuses);

        assertNotNull(result);
        List<Reports> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(1, resultList.size());
        Reports resultTrip = resultList.get(0);
        assertEquals("HRID123", resultTrip.getHumanReadableId());
        assertEquals("REQ123", resultTrip.getRequestHumanReadableId());
        assertEquals("01.01.2023", resultTrip.getDesiredDate());
        assertEquals(List.of("Start Address"), resultTrip.getRouteStart());
        assertEquals(List.of("Waypoint1", "Waypoint2"), resultTrip.getRouteWaypoints());
        assertEquals(List.of("End Address"), resultTrip.getRouteEnd());
        assertEquals(15, resultTrip.getExpectedWaitTime());
        assertEquals(10.5, resultTrip.getExpectedDistance());
        assertEquals(100.0, resultTrip.getMinRideDistanceCost());
        assertEquals(5.0, resultTrip.getWaitCostPerMin());
        assertEquals(10.0, resultTrip.getRideCostPerKm());
        assertEquals("NEW", resultTrip.getStatus());
        assertEquals("No comments", resultTrip.getComment());
        assertEquals("Business", resultTrip.getTripType());
        assertEquals("CONTRACT123", resultTrip.getContractNumber());
        assertEquals("Driver Name", resultTrip.getDriverFullName());
        assertEquals("A123BC", resultTrip.getVehicleNumber());
        assertEquals(List.of("Passenger One", "Passenger Two"), resultTrip.getPassengerFullNames());
        assertEquals("Author Name", resultTrip.getAuthorFullName());
        assertEquals("01.01.2023 12:00:00", resultTrip.getCreationTime());
    }

    @Test
    @DisplayName("Поиск поездки по ID - поездка найдена")
    void testFindTripFound() {
        UUID reportId = UUID.randomUUID();
        Reports tripsDto = new Reports();
        tripsDto.setHumanReadableId("HRID123");
        tripsDto.setRequestHumanReadableId("REQ123");
        tripsDto.setDesiredDate("01.01.2023");
        tripsDto.setRouteStart(List.of("Start Address"));
        tripsDto.setRouteWaypoints(List.of("Waypoint1", "Waypoint2"));
        tripsDto.setRouteEnd(List.of("End Address"));
        tripsDto.setExpectedWaitTime(15);
        tripsDto.setExpectedDistance(10.5);
        tripsDto.setMinRideDistanceCost(100.0);
        tripsDto.setWaitCostPerMin(5.0);
        tripsDto.setRideCostPerKm(10.0);
        tripsDto.setStatus("NEW");
        tripsDto.setComment("No comments");
        tripsDto.setTripType("Business");
        tripsDto.setContractNumber("CONTRACT123");
        tripsDto.setDriverFullName("Driver Name");
        tripsDto.setVehicleNumber("A123BC");
        tripsDto.setPassengerFullNames(List.of("Passenger One", "Passenger Two"));
        tripsDto.setAuthorFullName("Author Name");
        tripsDto.setCreationTime("01.01.2023 12:00:00");

        when(tripProvider.findById(eq(reportId)))
                .thenReturn(Optional.of(tripsDto));

        Reports resultTrip = reportService.find(reportId);

        assertNotNull(resultTrip);
        assertEquals("HRID123", resultTrip.getHumanReadableId());
        assertEquals("REQ123", resultTrip.getRequestHumanReadableId());
        assertEquals("01.01.2023", resultTrip.getDesiredDate());
        assertEquals(List.of("Start Address"), resultTrip.getRouteStart());
        assertEquals(List.of("Waypoint1", "Waypoint2"), resultTrip.getRouteWaypoints());
        assertEquals(List.of("End Address"), resultTrip.getRouteEnd());
        assertEquals(15, resultTrip.getExpectedWaitTime());
        assertEquals(10.5, resultTrip.getExpectedDistance());
        assertEquals(100.0, resultTrip.getMinRideDistanceCost());
        assertEquals(5.0, resultTrip.getWaitCostPerMin());
        assertEquals(10.0, resultTrip.getRideCostPerKm());
        assertEquals("NEW", resultTrip.getStatus());
        assertEquals("No comments", resultTrip.getComment());
        assertEquals("Business", resultTrip.getTripType());
        assertEquals("CONTRACT123", resultTrip.getContractNumber());
        assertEquals("Driver Name", resultTrip.getDriverFullName());
        assertEquals("A123BC", resultTrip.getVehicleNumber());
        assertEquals(List.of("Passenger One", "Passenger Two"), resultTrip.getPassengerFullNames());
        assertEquals("Author Name", resultTrip.getAuthorFullName());
        assertEquals("01.01.2023 12:00:00", resultTrip.getCreationTime());
    }

    @Test
    @DisplayName("Поиск поездки по ID - поездка не найдена")
    void testFindTripNotFound() {
        UUID reportId = UUID.randomUUID();

        when(tripProvider.findById(eq(reportId)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reportService.find(reportId);
        });
    }
}