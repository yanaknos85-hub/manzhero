package ru.sberbank.ditsib.transport.limits.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.core.convert.converter.Converter;
import ru.sberbank.ditsib.transport.limits.dto.v2.Period;

import java.io.IOException;

public class PeriodSerializer extends JsonSerializer<Period> implements Converter<Period, String> {

    private static final long serialVersionUID = 1L;


    @Override
    public void serialize(Period value, JsonGenerator gen, SerializerProvider sp) throws IOException {
        gen.writeString(value.name());
    }

    @Override
    public String convert(Period period) {
        return period.name();
    }
}
