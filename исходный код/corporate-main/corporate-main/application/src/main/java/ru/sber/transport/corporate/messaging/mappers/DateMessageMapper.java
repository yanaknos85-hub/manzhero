package ru.sber.transport.corporate.messaging.mappers;

import org.mapstruct.Mapper;

import java.time.*;

/**
 * Маппер дат
 */
@Mapper
public interface DateMessageMapper {

    /**
     * Конвертация сообщения в модель,
     *
     * @param source сообщение.
     * @return модель.
     */
    default OffsetDateTime toBusiness(Instant source) {
        return OffsetDateTime.ofInstant(source, ZoneOffset.UTC);
    }

}
