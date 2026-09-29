package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Сериализатор длительности в строку.
 */
public class DurationStringTimeConverter extends JsonSerializer<Duration> implements Converter<Duration, String> {
    
    @Override
    public void serialize(Duration duration, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeString(Optional.ofNullable(convert(duration)).orElse(0+""));
    }
    
    @Override
    public String convert(Duration duration) {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.MIDNIGHT.plus(duration.truncatedTo(ChronoUnit.SECONDS)));
    }
}
