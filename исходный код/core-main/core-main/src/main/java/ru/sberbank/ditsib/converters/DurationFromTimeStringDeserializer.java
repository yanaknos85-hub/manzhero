package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Десериализатор длительности из строки.
 */
public class DurationFromTimeStringDeserializer extends JsonDeserializer<Duration>
        implements Converter<String, Duration> {
    
    @Override
    public Duration deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        return convert(jp.readValueAs(String.class));
    }
    
    @Override
    public Duration convert(String source) {
        return Duration.ofSeconds(LocalTime.from(DateTimeFormatter.ofPattern("HH:mm:ss").parse(source)).toSecondOfDay());
    }
}
