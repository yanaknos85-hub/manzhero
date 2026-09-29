package ru.sberbank.ditsib.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;

import java.io.IOException;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

/**
 * Конвертер для разных форм записи ZonedDateTime
 */
public class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> implements
        Converter<String, ZonedDateTime>, Formatter<ZonedDateTime> {
    private final DateTimeFormatter dateTimeFormatter =
            new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                          // offset (hh:mm - "+00:00" when it's zero)
                                          .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
                                          // offset (hhmm - "+0000" when it's zero)
                                          .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
                                          // offset (hh - "Z" when it's zero)
                                          .optionalStart().appendOffset("+HH", "Z").optionalEnd()
                                          // create formatter
                                          .toFormatter();
    
    @Override
    public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        return convert(jsonParser.readValueAs(String.class));
    }
    
    @Override
    public ZonedDateTime convert(String s) {
        return ZonedDateTime.from(dateTimeFormatter.parse(s));
    }
    
    @Override
    public ZonedDateTime parse(String text, Locale locale) throws ParseException {
        
        return ZonedDateTime.from(dateTimeFormatter.parse(text));
    }
    
    @Override
    public String print(ZonedDateTime dateTime, Locale locale) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
    }
}
