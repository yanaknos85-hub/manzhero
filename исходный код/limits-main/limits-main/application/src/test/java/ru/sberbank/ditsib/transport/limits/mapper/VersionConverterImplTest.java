package ru.sberbank.ditsib.transport.limits.mapper;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dto.LimitTransferHistoryDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingPerPeriodV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;
import ru.sberbank.ditsib.transport.limits.dto.v2.Period;
import ru.sberbank.ditsib.transport.limits.dto.v2.Quarter;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.service.LimitService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка конвертера версий")
class VersionConverterImplTest {

    private final LimitService limitService = mock(LimitService.class);

    private final VersionConverter converter = new VersionConverterImpl(new PeriodMapperImpl(), new DateMapperImpl()) {
        @Override
        public LimitService limitService() {
            return limitService;
        }
    };

    @Test
    @DisplayName("Проверка конвертации в V1")
    void test_toV1Current() {
        assertThat(converter.toV1Current(null)).isNull();
        var source = Instancio.ofList(GetLimitSharingPerPeriodV2DTO.class)
            .set(Select.field(GetLimitSharingPerPeriodV2DTO::period), Period.create(LocalDate.now(ZoneOffset.UTC), Month.class))
            .create();

        var actual = converter.toV1Current(source);

        assertSoftly(soft -> {
            soft.assertThat(actual.getId()).isEqualTo(source.getFirst().id());
            soft.assertThat(actual.getSumResharingsPeriod()).isEqualTo(source.getFirst().sumResharingsPeriod());
            soft.assertThat(actual.getSum()).isEqualTo(source.getFirst().sum());
            soft.assertThat(actual.getSumReservedForCurrentPeriod()).isEqualTo(source.getFirst().sumReservedForCurrentPeriod());
            soft.assertThat(actual.getPeriodNumber()).isEqualTo(source.getFirst().period().ordinal());
            soft.assertThat(actual.getCreationTime()).isEqualTo(source.getFirst().creationTime());
            soft.assertThat(actual.getBalance()).isEqualTo(source.getFirst().balance());
            soft.assertThat(actual.getAuthor()).isEqualTo(source.getFirst().author());
        });
    }

    @Test
    @DisplayName("Конвертация истории. Месяц")
    void test_fromLimit_month() {
        var limit = Instancio.of(DepLimit.class)
            .set(Select.field(Limit::getLimitSharingType), LimitSharingType.MONTHLY)
            .create();
        var source = Instancio.of(LimitTransferHistoryDTO.class)
            .set(Select.field(LimitTransferHistoryDTO::getSourcePeriod), Instancio.create(Month.class).ordinal())
            .create();

        when(limitService.get(source.getSourceLimit())).thenReturn(Optional.of(limit));

        var actual = converter.fromSourceLimit(source);

        assertThat(actual.ordinal()).isEqualTo(source.getSourcePeriod());
    }

    @Test
    @DisplayName("Конвертация истории. Квартал")
    void test_fromLimit_quarter() {
        var limit = Instancio.of(DepLimit.class)
            .set(Select.field(Limit::getLimitSharingType), LimitSharingType.QUARTER)
            .create();
        var source = Instancio.of(LimitTransferHistoryDTO.class)
            .set(Select.field(LimitTransferHistoryDTO::getSourcePeriod), Instancio.create(Quarter.class).ordinal())
            .create();

        when(limitService.get(source.getSourceLimit())).thenReturn(Optional.of(limit));

        var actual = converter.fromSourceLimit(source);

        assertThat(actual.ordinal()).isEqualTo(source.getSourcePeriod());
    }

}