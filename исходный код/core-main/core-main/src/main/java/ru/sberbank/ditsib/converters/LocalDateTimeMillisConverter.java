package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.core.convert.converter.Converter;

import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Сериализатор даты-времени в миллисекунды.
 */
public class LocalDateTimeMillisConverter extends JsonSerializer<LocalDateTime>
        implements Converter<LocalDateTime, Long> {
    
    @Override
    public void serialize(
            @NotNull LocalDateTime value, JsonGenerator gen, SerializerProvider serializerProvider
                         ) throws IOException {
        var convertedValue = convert(value);
        if (convertedValue != null) {
            gen.writeNumber(convertedValue);
        } else {
            gen.writeNull();
        }
    }
    
    
    @Override
    public Long convert(@NotNull LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
