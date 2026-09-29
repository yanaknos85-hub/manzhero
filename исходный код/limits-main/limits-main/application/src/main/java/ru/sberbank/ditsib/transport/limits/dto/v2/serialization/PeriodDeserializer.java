package ru.sberbank.ditsib.transport.limits.dto.v2.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;
import ru.sberbank.ditsib.transport.limits.dto.v2.Period;
import ru.sberbank.ditsib.transport.limits.dto.v2.Quarter;

import java.io.IOException;

public class PeriodDeserializer extends JsonDeserializer<Period> {
    @Override
    public Period deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        var text = jsonParser.getText();
        if (text.startsWith("Q")) {
            return Quarter.valueOf(text);
        } else {
            return Month.valueOf(text);
        }
    }
}
