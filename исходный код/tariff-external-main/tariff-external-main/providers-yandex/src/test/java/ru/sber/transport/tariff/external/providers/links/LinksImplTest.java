package ru.sber.transport.tariff.external.providers.links;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.tariff.external.model.Type;
import ru.sber.transport.tariff.external.providers.Links;
import ru.sber.transport.tariff.external.providers.TestCoordinates;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_tariff_external")
@DisplayName("Проверка поставщика ссылок")
class LinksImplTest {

    private final Links links = new LinksImpl();

    @Test
    @DisplayName("Проверка поставщика ссылок")
    void test_get() {
        final var coordinates = List.of(
                new TestCoordinates(55.751244, 37.618423),
                new TestCoordinates(155.752244, 137.619423)
        );
        final var tariffType = Type.ECONOMY;
        assertThat(links.get(coordinates, tariffType))
                .isEqualTo(URI.create("https://3.redirect.appmetrica.yandex.com/route?start-lat=55.751244&start-lon=37.618423&end-lat=155.752244&end-lon=137.619423&tariffClass=econom&appmetrica_tracking_id=1178268795219780156"));
    }

}