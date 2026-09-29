package ru.sberbank.ditsib.transport.limits.dto.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SumDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return BigDecimal.valueOf(jsonParser.getValueAsLong()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                .stripTrailingZeros();
    }
}
