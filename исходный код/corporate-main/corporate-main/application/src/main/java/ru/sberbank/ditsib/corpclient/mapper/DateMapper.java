package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.sber.transport.corporate.sync.grpc.service.State;

import java.time.*;
import java.util.Objects;

@Mapper
public interface DateMapper {

    default LocalDateTime toDate(LocalDate source) {
        return LocalDateTime.of(source, LocalTime.of(0, 0));
    }

    @Named("fromString")
    default LocalDate fromString(String source) {
        if (Objects.isNull(source) || source.isEmpty()) return null;
        return LocalDate.parse(source);
    }

    default LocalDate toBusiness(State.Date source) {
        return LocalDate.of(source.getYear(), source.getMonth(), source.getDay());
    }

    default OffsetDateTime toBusiness(State.OffsetDateTime source) {
        var localTime = LocalTime.of(source.getHour(), source.getMinute(), source.getSecond(), source.getMillis());
        var localDate = toBusiness(source.getDate());
        var zoneOffset = ZoneOffset.of(source.getOffset());
        return OffsetDateTime.of(localDate, localTime, zoneOffset);
    }

    default LocalDate toBusiness(State.NullableDate source) {
        if (!source.hasValue()) {
            return null;
        }
        return toBusiness(source.getValue());
    }

    default OffsetDateTime toBusiness(Instant source) {
        return OffsetDateTime.ofInstant(source, ZoneOffset.UTC);
    }

}
