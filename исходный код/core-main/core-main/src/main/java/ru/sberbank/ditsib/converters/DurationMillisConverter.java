package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * Сериализатор длительности в миллисекунды.
 */
public class DurationMillisConverter extends JsonSerializer<Duration> implements Converter<Duration, Long> {
    
    @Override
    public void serialize(Duration duration, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeNumber(Optional.ofNullable(duration).map(this::convert).orElse(0L));
    }
    
    @Override
    public Long convert(Duration duration) {
        return duration.toMillis();
    }
}
