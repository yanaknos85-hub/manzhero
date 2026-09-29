package ru.sber.transport.tariff.external.providers.serializing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.tariff.external.providers.model.YandexClass;
import ru.sber.transport.tariff.external.providers.tariff.serializing.YandexClassDeserializer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_tariff_external")
@DisplayName("Проверка десериализатора типа тарифа")
class YandexClassDeserializerTest {

    private final YandexClassDeserializer deserializer = new YandexClassDeserializer();

    @Test
    @DisplayName("Проверка десериализации")
    void test_deserialize() throws IOException {
        final var parser = mock(JsonParser.class);
        final var type = Instancio.create(YandexClass.class);
        final var context = mock(DeserializationContext.class);

        when(parser.readValueAs(String.class)).thenReturn(type.name().toLowerCase());

        assertThat(deserializer.deserialize(parser, context)).isEqualTo(type);
    }

}