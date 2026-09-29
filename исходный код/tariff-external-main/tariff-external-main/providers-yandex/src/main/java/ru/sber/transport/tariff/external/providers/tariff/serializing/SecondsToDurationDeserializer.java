package ru.sber.transport.tariff.external.providers.tariff.serializing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

/**
 * Десериализация секунд в продолжительность
 */
public class SecondsToDurationDeserializer extends JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return Duration.ofSeconds(jsonParser.readValueAs(Long.class));
    }

}
