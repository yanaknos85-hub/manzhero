package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Маппер дат.
 */
@Mapper
public interface DateGrpcMapper {

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default LocalDate toBusiness(State.Date source) {
        return LocalDate.of(source.getYear(), source.getMonth(), source.getDay());
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default LocalDate toBusiness(Import.Date source) {
        return LocalDate.of(source.getYear(), source.getMonth(), source.getDay());
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default LocalDate toBusiness(Import.NullableDate source) {
        if (source.hasValue()) {
            return toBusiness(source.getValue());
        }
        return null;
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default OffsetDateTime toBusiness(State.OffsetDateTime source) {
        return Optional.ofNullable(source)
                .map(it -> OffsetDateTime.of(
                        it.getDate().getYear(),
                        it.getDate().getMonth(),
                        it.getDate().getDay(),
                        it.getHour(),
                        it.getMinute(),
                        it.getSecond(),
                        it.getMillis(),
                        ZoneOffset.of(it.getOffset())
                ))
                .orElse(null);
    }

}
