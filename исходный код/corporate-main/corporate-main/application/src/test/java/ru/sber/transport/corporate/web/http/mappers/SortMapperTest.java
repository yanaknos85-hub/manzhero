package ru.sber.transport.corporate.web.http.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.web.model.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка маппера сортировки")
class SortMapperTest {

    private final SortMapper mapper = new SortMapperImpl();

    @Test
    @DisplayName("Проверка маппера сортировки")
    void test_map() {
        assertThat(mapper.toDirection(true)).isEqualTo(Sort.DirectionEnum.ASC);
        assertThat(mapper.toDirection(false)).isEqualTo(Sort.DirectionEnum.DESC);
    }

}