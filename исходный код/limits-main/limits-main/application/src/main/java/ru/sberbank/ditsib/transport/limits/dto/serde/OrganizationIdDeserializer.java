package ru.sberbank.ditsib.transport.limits.dto.serde;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class OrganizationIdDeserializer extends JsonDeserializer<List<UUID>> {

    @Override
    public List<UUID> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (JsonToken.START_ARRAY == jsonParser.getCurrentToken()) {
            var result = new LinkedList<UUID>();
            var token = jsonParser.nextToken();
            while (token != JsonToken.END_ARRAY) {
                if (JsonToken.VALUE_STRING == token) {
                    var value = jsonParser.getValueAsString();
                    try {
                        result.add(UUID.fromString(value));
                    } catch (IllegalArgumentException e) {
                        throw new JsonParseException(jsonParser, "%s is not a UUID".formatted(value), e);
                    }
                }
                token = jsonParser.nextToken();
            }
            return result;
        }
        return List.of(deserializationContext.readValue(jsonParser, UUID.class));
    }
}
