package ru.sber.transport.tariff.external.providers.tariff.serializing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ru.sber.transport.tariff.external.providers.model.YandexClass;

import java.io.IOException;

/**
 * Десериализация типа тарифа яндекса
 */
public class YandexClassDeserializer extends JsonDeserializer<YandexClass> {

    @Override
    public YandexClass deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return YandexClass.valueOf(jsonParser.readValueAs(String.class).toUpperCase());
    }

}
