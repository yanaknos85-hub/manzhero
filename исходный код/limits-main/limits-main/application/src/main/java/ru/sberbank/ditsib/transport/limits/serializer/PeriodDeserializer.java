package ru.sberbank.ditsib.transport.limits.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.core.convert.converter.Converter;
import ru.sberbank.ditsib.transport.limits.dto.v2.Period;

import java.io.IOException;

public class PeriodDeserializer extends JsonDeserializer<Period> implements Converter<String, Period> {

    private static final long serialVersionUID = 1L;


    @Override
    public Period deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return Period.fromString(jsonParser.getValueAsString());
    }

    @Override
    public Period convert(String source) {
        return Period.fromString(source);
    }
}
