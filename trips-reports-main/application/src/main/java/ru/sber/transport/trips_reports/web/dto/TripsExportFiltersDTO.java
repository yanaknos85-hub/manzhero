package ru.sber.transport.trips_reports.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripsExportFiltersDTO {

    private UUID contractorId;

    private Duration duration;

    private List<TripStatus> statuses;

    @Getter
    @Setter
    public static class Duration{

        private OffsetDateTime start;

        private OffsetDateTime end;

    }

}
