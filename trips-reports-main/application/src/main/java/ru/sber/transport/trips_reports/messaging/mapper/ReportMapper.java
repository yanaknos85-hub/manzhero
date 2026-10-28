package ru.sber.transport.trips_reports.messaging.mapper;

import org.mapstruct.*;
import ru.sber.transport.trips_reports.database.trips_reports.tables.records.ReportRecord;
import ru.sber.transport.trips_reports.model.Reports;


import ru.sber.transport.trips_reports.web.dto.ReportsDto;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ReportMapper {

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "tripHumanReadableId", target = "humanReadableId"),
            @Mapping(source = "requestIds", target = "requestHumanReadableId"),
            @Mapping(source = "author", target = "authorFullName"),
            @Mapping(source = "driver", target = "driverFullName"),
            @Mapping(source = "creationTime", target = "creationTime", qualifiedByName = "offsetDateTimeToString"),
            @Mapping(target = "routeStart", ignore = true),
            @Mapping(target = "routeWaypoints", ignore = true),
            @Mapping(target = "routeEnd", ignore = true),
            @Mapping(target = "passengerFullNames", ignore = true)
    })
    Reports toModel(ReportRecord report);

    @Mapping(source = "reports.expectedTime", target = "expectedTime", qualifiedByName = "millisToMinutes")
    @Mapping(source = "reports.factTime", target = "factTime", qualifiedByName = "millisToMinutes")
    @Mapping(source = "reports.factWaitTime", target = "factWaitTime", qualifiedByName = "millisToMinutes")
    ReportsDto toDto(Reports reports);

    @AfterMapping
    default void afterMapping(@MappingTarget Reports reports, ReportRecord record) {
        reports.setDesiredDate(offsetDateTimeToString(record.getExpectedStartTime()));
        reports.setRouteStart(parseAddress(record.getRouteStart()));
        reports.setRouteWaypoints(parseAddress(record.getRouteWaypoints()));
        reports.setRouteEnd(parseAddress(record.getRouteEnd()));
        reports.setPassengerFullNames(parsePassengers(record.getPassenger()));
    }

    @Named("offsetDateTimeToString")
    default String offsetDateTimeToString(OffsetDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    default List<String> parseAddress(String addressJson) {
        if (addressJson == null) return Collections.emptyList();
        return List.of(addressJson);
    }

    default List<String> parsePassengers(String passengersJson) {
        if (passengersJson == null) return Collections.emptyList();
        return List.of(passengersJson);
    }

    @Named("millisToMinutes")
    default Long millisToMinutes(Long millis){
        return millis == null ? null : Duration.ofMillis(millis).toMinutes();
    }

}
