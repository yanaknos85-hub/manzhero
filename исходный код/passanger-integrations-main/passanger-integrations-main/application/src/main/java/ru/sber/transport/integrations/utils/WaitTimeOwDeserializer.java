package ru.sber.transport.integrations.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.StringUtils;
import ru.sber.transport.integrations.exception.WaitTimeOwParseException;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Десериализация строки с исключением спецсимволов.
 */
public class WaitTimeOwDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        var value = jsonParser.getText();
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            try {
                return (int) Duration.ofSeconds(LocalTime.parse(value).toSecondOfDay()).getSeconds();
            } catch (Exception ex) {
                throw new WaitTimeOwParseException(value);
            }
        }
    }
}