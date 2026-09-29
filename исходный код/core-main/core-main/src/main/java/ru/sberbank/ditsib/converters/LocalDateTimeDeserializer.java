package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Json десериализатор для LocalDateTime
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
        implements Converter<String, LocalDateTime> {
    
    
    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        return convert(jp.readValueAs(String.class));
    }
    
    @Override
    public LocalDateTime convert(String s) {
        return LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s));
    }
}