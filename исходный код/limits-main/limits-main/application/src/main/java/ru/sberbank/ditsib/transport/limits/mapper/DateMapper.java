package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Веб-маппер для работы с датами
 */
@Mapper
public interface DateMapper {

    /**
     * Преобразовать дату и время в веб-слое
     *
     * @param source дата и время
     * @return дата и время
     */
    default LocalDateTime toWeb(OffsetDateTime source) {
        if (source == null) {
            return null;
        } else {
            return source.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        }
    }

}
