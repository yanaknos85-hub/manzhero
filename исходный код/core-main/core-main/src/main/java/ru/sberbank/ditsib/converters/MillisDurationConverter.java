package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.Duration;

/**
 * Десериализатор длительности из миллисекунд.
 */
public class MillisDurationConverter extends JsonDeserializer<Duration> implements Converter<Long, Duration> {
    
    @Override
    public Duration convert(@NonNull Long millis) {
        return Duration.ofMillis(millis);
    }
    
    @Override
    public Duration deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        return convert(jsonParser.getLongValue());
    }
}
