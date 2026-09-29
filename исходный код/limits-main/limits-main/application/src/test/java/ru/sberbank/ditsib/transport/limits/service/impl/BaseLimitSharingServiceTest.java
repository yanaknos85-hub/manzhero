package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingPerPeriodRepository;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Month;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка базового сервиса по работе с распределениями")
class BaseLimitSharingServiceTest {

    private final LimitSharingPerPeriodRepository repository = mock(LimitSharingPerPeriodRepository.class);

    private final BaseLimitSharingService<Month> service = new LimitSharingPerMonthServiceImpl(repository);

    @Test
    @DisplayName("Получение по идентификатору")
    void test_get() {
        var id = UUID.randomUUID();
        var sharing = Instancio.create(LimitSharingPerPeriod.class);

        when(repository.findById(id)).thenReturn(Optional.of(sharing));

        var actual = service.get(id);
        assertThat(actual).hasValue(sharing);
    }

    @Test
    @DisplayName("Получение всех")
    void test_getAll() {
        var sharings = Instancio.createList(LimitSharingPerPeriod.class);

        when(repository.findAll()).thenReturn(sharings);

        var actual = service.getAll();
        assertThat(actual).hasSameElementsAs(sharings);
    }

    @Test
    @DisplayName("Получение всех периодов по sharing")
    void test_getPeriodsBySharings() {
        var limitSharingPerPeriod = Instancio.createList(LimitSharingPerPeriod.class);
        var sharings = Instancio.createList(LimitSharing.class);

        when(repository.findByLimitSharingIdsInAndPeriodData(anyCollection(), any())).thenReturn(limitSharingPerPeriod);

        var actual = service.getPeriodsBySharings(sharings, Month.valueOf(LocalDate.now().getMonth()));
        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(limitSharingPerPeriod.size());
    }

    @Test
    @DisplayName("Получение всех периодов по пустой коллекции sharings")
    void test_getPeriodsBySharingsIsEmpty() {
        var actual = service.getPeriodsBySharings(Collections.emptyList(), Month.JANUARY);

        assertThat(actual)
                .isNotNull()
                .isEmpty();
    }
}