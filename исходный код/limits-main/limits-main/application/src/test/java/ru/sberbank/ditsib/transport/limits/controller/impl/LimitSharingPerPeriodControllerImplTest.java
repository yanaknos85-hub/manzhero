package ru.sberbank.ditsib.transport.limits.controller.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.controller.LimitSharingPerPeriodController;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitSharingController;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingPerPeriodV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;
import ru.sberbank.ditsib.transport.limits.mapper.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка контроллера шар за период")
class LimitSharingPerPeriodControllerImplTest {

    private final LimitSharingController baseController = mock(LimitSharingController.class);

    private final PeriodMapper periodMapper = new PeriodMapperImpl();

    private final VersionConverter versionConverter = new VersionConverterImpl(periodMapper, new DateMapperImpl());

    private final LimitSharingPerPeriodController controller = new LimitSharingPerPeriodControllerImpl(new LimitSharingMapperImpl(new EmployeeMapperImpl(), periodMapper), baseController, versionConverter);

    @Test
    @DisplayName("Получение данных по шаре")
    void test_getByLimitSharing() {
        var limitSharingId = UUID.randomUUID();
        var periods = Instancio.ofList(GetLimitSharingPerPeriodV2DTO.class).set(Select.field(GetLimitSharingPerPeriodV2DTO::period), Instancio.create(Month.class)).create();
        var value = Instancio.of(GetLimitSharingV2DTO.class)
            .set(Select.field(GetLimitSharingV2DTO::sharings), periods)
            .create();

        when(baseController.get(limitSharingId)).thenReturn(value);

        var actualList = controller.getByLimitSharing(limitSharingId);

        assertThat(actualList).hasSameSizeAs(periods);

        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = periods.get(i);

            assertSoftly(soft -> {
                soft.assertThat(actual.getAuthor()).isEqualTo(expected.author());
                soft.assertThat(actual.getBalance()).isEqualTo(expected.balance());
                soft.assertThat(actual.getCreationTime()).isEqualTo(expected.creationTime());
                soft.assertThat(actual.getSum()).isEqualTo(expected.sum());
                soft.assertThat(actual.getSumReservedForCurrentPeriod()).isEqualTo(expected.sumReservedForCurrentPeriod());
                soft.assertThat(actual.getSumResharingsPeriod()).isEqualTo(expected.sumResharingsPeriod());
                soft.assertThat(actual.getPeriodNumber()).isEqualTo(expected.period().ordinal());
            });
        }
    }

}