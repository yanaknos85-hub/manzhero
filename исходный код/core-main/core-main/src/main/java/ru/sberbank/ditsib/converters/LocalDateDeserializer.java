package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Json десериализатор для LocalDate
 */
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> implements Converter<String, LocalDate> {
    
    private static final long serialVersionUID = 1L;
    
    
    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        return LocalDate.parse(jp.readValueAs(String.class));
    }
    
    @Override
    public LocalDate convert(String s) {
        return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(s));
    }
}