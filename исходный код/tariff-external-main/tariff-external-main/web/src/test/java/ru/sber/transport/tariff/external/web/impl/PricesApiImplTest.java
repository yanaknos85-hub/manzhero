package ru.sber.transport.tariff.external.web.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.HttpStatus;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.tariff.external.business.Tariffs;
import ru.sber.transport.tariff.external.model.Tariff;
import ru.sber.transport.tariff.external.model.Type;
import ru.sber.transport.web.api.PricesApi;
import ru.sber.transport.web.model.TariffType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_tariff_external")
@DisplayName("Проверка делегата цен")
class PricesApiImplTest {

    private final Tariffs tariffs = mock(Tariffs.class);

    private final PricesApi pricesApiDelegate = new PricesApiImpl(tariffs);

    @Test
    @DisplayName("Получение списка тарифов")
    void test_get() {
        final var tariffTypes = Instancio.createList(TariffType.class);
        final var tariffData = new ArrayList<Tariff>(Instancio.createList(TestTariff.class));

        when(tariffs.get(anyList(), eq(tariffTypes.stream().map(Enum::name).map(Type::valueOf).toList())))
                .thenReturn(tariffData);

        final var actualResponse = pricesApiDelegate.get("50.34,30.12;50.35,30.13", tariffTypes);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var actualList = actualResponse.getBody();

        assertThat(actualList).isNotNull().hasSameSizeAs(tariffData);

        for (var i = 0; i < tariffData.size(); i++) {
            final var actual = actualList.get(i);
            final var expected = tariffData.get(i);
            assertSoftly(it -> {
                it.assertThat(actual.getPrice()).isEqualTo(expected.price());
                it.assertThat(actual.getDistance().longValue()).isEqualTo(expected.distance());
                it.assertThat(actual.getTime()).isEqualTo(expected.time().toString());
                it.assertThat(actual.getWaitTime()).isEqualTo(expected.waitTime().toString());
                it.assertThat(actual.getType().name()).isEqualTo(expected.type().name());
            });
        }
    }

    private record TestTariff(Type type, long distance, BigDecimal price, Duration time, Duration waitTime) implements Tariff { }

}