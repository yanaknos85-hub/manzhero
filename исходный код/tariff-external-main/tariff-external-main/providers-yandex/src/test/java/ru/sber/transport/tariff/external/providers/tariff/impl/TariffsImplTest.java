package ru.sber.transport.tariff.external.providers.tariff.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.tariff.external.model.Type;
import ru.sber.transport.tariff.external.providers.Tariffs;
import ru.sber.transport.tariff.external.providers.TestCoordinates;
import ru.sber.transport.tariff.external.providers.tariff.config.ExchangeProperties;
import ru.sber.transport.tariff.external.providers.tariff.config.RequestProperties;
import ru.sber.transport.tariff.external.providers.model.YandexClass;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_tariff_external")
@DisplayName("Проверка поставщика тарифов")
class TariffsImplTest {

    private final ExchangeProperties properties = mock(ExchangeProperties.class);

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final Tariffs tariffs = new TariffsImpl(properties, restTemplate);

    @Test
    @DisplayName("Получение списка тарифов по координатам")
    void test_get() {
        final var requestProperties = mock(RequestProperties.class);
        final var coordinates = Instancio.createList(TestCoordinates.class);
        final var tariffTypes = Instancio.createList(Type.class);
        final var response = ResponseEntity.ok(
                new YandexTariff(61529.771101536542, List.of(
                        new Option(YandexClass.ECONOM, 239D, Duration.ofSeconds(3816)),
                        new Option(YandexClass.VIP, 299D, Duration.ofSeconds(13816))
                ), Duration.ofSeconds(23816))
        );

        when(requestProperties.apiKey()).thenReturn("api_key");

        when(properties.getRequest()).thenReturn(requestProperties);
        when(restTemplate.exchange(any(RequestEntity.class), eq(YandexTariff.class))).thenReturn(response);

        final var actualList = tariffs.get(coordinates, tariffTypes);

        assertThat(actualList).hasSize(2);

        assertThat(actualList.get(0).type()).isEqualTo(Type.ECONOMY);
        assertThat(actualList.get(0).distance()).isEqualTo(61529);
        assertThat(actualList.get(0).price().stripTrailingZeros()).isEqualTo(BigDecimal.valueOf(239).stripTrailingZeros());
        assertThat(actualList.get(0).time()).isEqualTo(Duration.ofSeconds(23816));
        assertThat(actualList.get(0).waitTime()).isEqualTo(Duration.ofSeconds(3816));

        assertThat(actualList.get(1).type()).isEqualTo(Type.VIP);
        assertThat(actualList.get(1).distance()).isEqualTo(61529);
        assertThat(actualList.get(1).price().stripTrailingZeros()).isEqualTo(BigDecimal.valueOf(299).stripTrailingZeros());
        assertThat(actualList.get(1).time()).isEqualTo(Duration.ofSeconds(23816));
        assertThat(actualList.get(1).waitTime()).isEqualTo(Duration.ofSeconds(13816));
    }
}