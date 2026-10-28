package ru.sber.transport.trips_reports.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.trips_reports.database.trips_reports.tables.records.ReportRecord;
import ru.sber.transport.trips_reports.messaging.mapper.ReportMapper;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.web.dto.ReportsDto;

class ReportMapperTest {

    private ReportMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ReportMapper.class);
    }

    @Test
    void testToModel() {
        ReportRecord record = new ReportRecord();
        record.setTripHumanReadableId("ОТЧЕТ123");
        record.setRequestIds("ЗАПРОС123");
        record.setRouteStart("Начало маршрута");
        record.setRouteEnd("Конец маршрута");
        record.setExpectedWaitTime(15L);
        record.setExpectedDistance(120.5);
        record.setStatus("В ПРОЦЕССЕ");
        record.setComment("Комментарий");
        record.setTripType("ЛИЧНЫЙ");
        record.setDriver("Иван Иванов");
        record.setVehicleNumber("АА123АА");
        record.setPassenger("Пассажир 1");
        record.setAuthor("Автор Отчета");
        record.setCreationTime(OffsetDateTime.parse("2023-10-01T09:00:00+00:00"));

        Reports model = mapper.toModel(record);

        assertEquals("ОТЧЕТ123", model.getHumanReadableId());
        assertEquals("ЗАПРОС123", model.getRequestHumanReadableId());
        assertEquals(List.of("Начало маршрута"), model.getRouteStart());
        assertEquals(List.of("Конец маршрута"), model.getRouteEnd());
        assertEquals(15, model.getExpectedWaitTime());
        assertEquals(120.5, model.getExpectedDistance());
        assertEquals("В ПРОЦЕССЕ", model.getStatus());
        assertEquals("Комментарий", model.getComment());
        assertEquals("ЛИЧНЫЙ", model.getTripType());
        assertEquals("Иван Иванов", model.getDriverFullName());
        assertEquals("АА123АА", model.getVehicleNumber());
        assertEquals(List.of("Пассажир 1"), model.getPassengerFullNames());
        assertEquals("Автор Отчета", model.getAuthorFullName());
        assertEquals("2023-10-01T09:00:00Z", model.getCreationTime());
    }

    @Test
    void testToDto() {
        Reports model = new Reports();
        model.setHumanReadableId("ОТЧЕТ124");
        model.setRequestHumanReadableId("ЗАПРОС124");
        model.setDesiredDate("02.10.2023");
        model.setRouteStart(List.of("Начало маршрута B"));
        model.setRouteWaypoints(List.of("Промежуточная точка B1"));
        model.setRouteEnd(List.of("Конец маршрута B"));
        model.setExpectedWaitTime(20);
        model.setExpectedDistance(150.75);
        model.setMinRideDistanceCost(600.0);
        model.setWaitCostPerMin(25.0);
        model.setRideCostPerKm(35.0);
        model.setStatus("ЗАВЕРШЕН");
        model.setComment("Задержка");
        model.setTripType("ДЕЛОВОЙ");
        model.setContractNumber("ДОГОВОР124");
        model.setDriverFullName("Петр Петров");
        model.setVehicleNumber("ББ234ББ");
        model.setPassengerFullNames(List.of("Пассажир 2", "Пассажир 3"));
        model.setAuthorFullName("Автор Отчета B");
        model.setCreationTime("02.10.2023 11:00:00");
        model.setExpectedTime(60000L);

        ReportsDto dto = mapper.toDto(model);

        assertEquals("ОТЧЕТ124", dto.humanReadableId());
        assertEquals("ЗАПРОС124", dto.requestHumanReadableId());
        assertEquals("02.10.2023", dto.desiredDate());
        assertEquals(List.of("Начало маршрута B"), dto.routeStart());
        assertEquals(List.of("Промежуточная точка B1"), dto.routeWaypoints());
        assertEquals(List.of("Конец маршрута B"), dto.routeEnd());
        assertEquals(20, dto.expectedWaitTime());
        assertEquals(150.75, dto.expectedDistance());
        assertEquals(600.0, dto.minRideDistanceCost());
        assertEquals(25.0, dto.waitCostPerMin());
        assertEquals(35.0, dto.rideCostPerKm());
        assertEquals("ЗАВЕРШЕН", dto.status());
        assertEquals("Задержка", dto.comment());
        assertEquals("ДЕЛОВОЙ", dto.tripType());
        assertEquals("ДОГОВОР124", dto.contractNumber());
        assertEquals("Петр Петров", dto.driverFullName());
        assertEquals("ББ234ББ", dto.vehicleNumber());
        assertEquals(List.of("Пассажир 2", "Пассажир 3"), dto.passengerFullNames());
        assertEquals("Автор Отчета B", dto.authorFullName());
        assertEquals("02.10.2023 11:00:00", dto.creationTime());
        assertEquals(1L, dto.expectedTime());
    }


    @Test
    @DisplayName("Проверка конвертации OffsetDateTime в строку")
    void testOffsetDateTimeToString() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2023-10-01T09:00:00+00:00");
        String formatted = mapper.offsetDateTimeToString(dateTime);
        assertThat(formatted).isEqualTo("2023-10-01T09:00:00Z");

        assertThat(mapper.offsetDateTimeToString(null)).isNull();
    }

    @Test
    @DisplayName("Проверка парсинга адреса")
    void testParseAddress() {
        String addressJson = "{\"address\":\"Start Point A\"}";
        List<String> addresses = mapper.parseAddress(addressJson);
        assertThat(addresses).containsExactly(addressJson);

        assertThat(mapper.parseAddress(null)).isEmpty();
    }

    @Test
    @DisplayName("Проверка парсинга пассажиров")
    void testParsePassengers() {
        String passengersJson = "[\"Passenger A1\"]";
        List<String> passengers = mapper.parsePassengers(passengersJson);
        assertThat(passengers).containsExactly(passengersJson);

        assertThat(mapper.parsePassengers(null)).isEmpty();
    }
}