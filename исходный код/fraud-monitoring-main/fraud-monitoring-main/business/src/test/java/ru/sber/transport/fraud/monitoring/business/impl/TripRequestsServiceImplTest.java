package ru.sber.transport.fraud.monitoring.business.impl;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.model.TripRequestDataWithMessages;
import ru.sber.transport.fraud.monitoring.providers.TripRequestsDatabaseProvider;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка сервиса заявок на поездку")
class TripRequestsServiceImplTest {

    private final TripRequestsDatabaseProvider tripRequestsDatabaseProvider = mock(TripRequestsDatabaseProvider.class);

    private final TripRequestsServiceImpl tripRequestsService = new TripRequestsServiceImpl(
            tripRequestsDatabaseProvider,
            null,
            null,
            null
    );

    @Test
    @DisplayName("get возвращает заявку с нарушениями, если она найдена в БД")
    void test_get_returnsTripRequestDataWithMessagesWhenFound() {
        final var id = UUID.randomUUID();
        final var data = mock(TripRequestDataWithMessages.class);

        when(tripRequestsDatabaseProvider.getWithMessages(id))
                .thenReturn(Optional.of(data));

        final var result = tripRequestsService.get(id);

        assertThat(result).isPresent();
        assertThat(result).containsSame(data);
        verify(tripRequestsDatabaseProvider).getWithMessages(id);
    }

    @Test
    @DisplayName("get возвращает пустой Optional, если заявка не найдена в БД")
    void test_get_returnsEmptyWhenNotFound() {
        final var id = UUID.randomUUID();

        when(tripRequestsDatabaseProvider.getWithMessages(id))
                .thenReturn(Optional.empty());

        final var result = tripRequestsService.get(id);

        assertThat(result).isEmpty();
        verify(tripRequestsDatabaseProvider).getWithMessages(id);
    }
}