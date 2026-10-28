package ru.sber.transport.trips_reports.web.service.impl.file_resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.trips_reports.providers.trips.TripProvider;
import ru.sber.transport.trips_reports.web.dto.TripsExportDto;
import ru.sber.transport.trips_reports.web.dto.TripsExportFiltersDTO;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
class ReportResolverImpl implements DataExporter<TripsExportDto> {
    private final TripProvider tripProvider;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @NotNull
    @Override
    public List<TripsExportDto> exportData(Map<String, ?> map, JwtAuthenticationToken jwtAuthenticationToken) {
        TripsExportFiltersDTO filters = null;
        if (map.containsKey("filters")) {
            var encodedFilters = map.get("filters").toString();
            var stringFilters = new String(Base64.getDecoder().decode(encodedFilters), StandardCharsets.UTF_8);
            filters = objectMapper.readValue(stringFilters, TripsExportFiltersDTO.class);
        }
        var trips = tripProvider.findAllByFilters(filters);
        var result = new LinkedList<TripsExportDto>();
        for (var i = 0; i < trips.size(); i++) {
            var item = trips.get(i);
            var factStartTime = item.getFactStartTime() != null ? item.getFactStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(item.getTimeZone()).normalized().getId())) : null;
            var factStartDate = factStartTime != null ? factStartTime.toLocalDate() : null;
            var factStartLocalTime = factStartTime != null ? factStartTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES) : null;
            var expectedStartTime = item.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(item.getTimeZone()).normalized().getId()));
            var creationDateTime = item.getCreationTime() != null ? item.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(item.getTimeZone()).normalized().getId())) : null;
            var creationDate = creationDateTime != null ? creationDateTime.toLocalDate() : null;
            var creationTime = creationDateTime != null ? creationDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES) : null;
            var expectedTime = item.getExpectedTime() != null ? Duration.ofMillis(item.getExpectedTime()).toMinutes() : null;
            var expectedWaitTime = item.getExpectedWaitTime() != null ? Duration.ofMillis(item.getExpectedWaitTime()).toMinutes() : null;
            var factTime = item.getFactTime() != null ? Duration.ofMillis(item.getFactTime()).toMinutes() : null;
            var factWaitTime = item.getFactWaitTime() != null ? Duration.ofMillis(item.getFactWaitTime()).toMinutes() : null;
            result.add(new TripsExportDto(
                    i + 1,
                    item.getTripHumanReadableId(),
                    item.getRequestIds(),
                    creationDate,
                    creationTime,
                    expectedStartTime.toLocalDate(),
                    expectedStartTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES),
                    factStartDate,
                    factStartLocalTime,
                    item.getRouteStart(),
                    item.getRouteWaypoints(),
                    item.getRouteEnd(),
                    item.getCheckinQuantity(),
                    item.getCheckinStatuses(),
                    item.getCheckinWaypointConfirmation(),
                    item.getStatus(),
                    item.getComment(),
                    item.getPassenger(),
                    item.getVehicleNumber(),
                    item.getDriver(),
                    item.getExpectedDistance(),
                    expectedTime,
                    expectedWaitTime,
                    item.getExpectedCost(),
                    item.getFactDistance(),
                    factTime,
                    factWaitTime,
                    item.getTripType(),
                    item.getDispatcherName(),
                    item.getDispatcherPhone(),
                    item.getTaxiClass(),
                    item.getPassengerCount()
            ));
        }
        return result;
    }
}
