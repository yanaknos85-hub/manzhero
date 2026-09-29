package ru.sberbank.ditsib.transport.limits.dto.v2.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.sberbank.ditsib.transport.limits.dto.v2.Period;

import java.io.IOException;

public class PeriodSerializer extends JsonSerializer<Period> {

    @Override
    public void serialize(Period period, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (period != null) {
            var name = period.name();
            if (name != null) {
                jsonGenerator.writeString(name);
            }
        }
    }
}
