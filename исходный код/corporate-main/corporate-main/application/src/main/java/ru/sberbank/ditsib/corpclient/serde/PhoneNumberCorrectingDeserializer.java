package ru.sberbank.ditsib.corpclient.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Десериализатор номеров телефонов с корректировкой номера.
 */
public class PhoneNumberCorrectingDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var mobilePhone = p.getValueAsString();
        if (StringUtils.hasText(mobilePhone)) {
            return mobilePhone.startsWith("8") ? mobilePhone.replaceFirst("8", "+7") : mobilePhone;
        } else {
            return null;
        }
    }

}
