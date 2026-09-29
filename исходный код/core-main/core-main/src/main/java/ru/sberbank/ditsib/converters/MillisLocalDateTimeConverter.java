package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Десериализатор даты-времени из миллисекунд.
 */
public class MillisLocalDateTimeConverter extends JsonDeserializer<LocalDateTime>
        implements Converter<Long, LocalDateTime> {
    
    @Override
    public LocalDateTime convert(@NonNull Long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of(ZoneOffset.UTC.getId()));
    }
    
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        return convert(jsonParser.getLongValue());
    }
}
