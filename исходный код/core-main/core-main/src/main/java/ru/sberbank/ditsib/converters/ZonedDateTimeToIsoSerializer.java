package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Json сериализатор для LocalDate
 */
public class ZonedDateTimeToIsoSerializer extends JsonSerializer<ZonedDateTime>
        implements Converter<ZonedDateTime, String> {
    
    @Override
    public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
        gen.writeString(convert(value));
    }
    
    @Override
    public String convert(ZonedDateTime dateTime) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
    }
}