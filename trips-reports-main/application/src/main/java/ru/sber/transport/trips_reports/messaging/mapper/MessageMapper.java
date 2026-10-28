package ru.sber.transport.trips_reports.messaging.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sber.transport.trips_reports.database.trips_reports.tables.records.ReportRecord;

import java.time.Duration;

@Mapper
public interface MessageMapper {
    ReportRecord toEntity(TripReportMessage message);

    default Long durationToLong(Duration duration) {
        return duration != null ? duration.toMillis() : null;
    }


}
