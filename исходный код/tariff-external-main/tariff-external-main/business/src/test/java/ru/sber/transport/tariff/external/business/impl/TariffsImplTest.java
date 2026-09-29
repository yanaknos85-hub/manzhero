package ru.sber.transport.tariff.external.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.tariff.external.model.Coordinates;
import ru.sber.transport.tariff.external.model.Tariff;
import ru.sber.transport.tariff.external.model.Type;
import ru.sber.transport.tariff.external.providers.Tariffs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_tariff_external")
@DisplayName("Проверка бизнес-кейсов сервиса тарифов")
class TariffsImplTest {

    private final Tariffs provider = mock(Tariffs.class);

    private final ru.sber.transport.tariff.external.business.Tariffs tariffs = new TariffsImpl(provider);

    @Test
    @DisplayName("Получение списка тарифов")
    void test_get() {
        final var coordinates = Instancio.createList(TestCoordinates.class);
        final var tariffTypes = Instancio.createList(Type.class);
        final var tariffs = Instancio.createList(Tariff.class);

        when(provider.get(coordinates, tariffTypes)).thenReturn(tariffs);

        assertThat(this.tariffs.get(coordinates, tariffTypes)).isEqualTo(tariffs);
    }

    private record TestCoordinates(double latitude, double longitude) implements Coordinates { }

}