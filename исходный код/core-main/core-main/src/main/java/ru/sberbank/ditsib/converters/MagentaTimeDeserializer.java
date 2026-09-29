package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * Десериализатор времени из строки.
 */
public class MagentaTimeDeserializer extends JsonDeserializer<ZonedDateTime>
        implements Converter<String, ZonedDateTime> {

    @Override
    public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        return convert(jsonParser.readValueAs(String.class));
    }
    
    @Override
    public ZonedDateTime convert(@NonNull String s) {
        return ZonedDateTime.parse(s);
    }
}
