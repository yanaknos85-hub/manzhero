package ru.sber.transport.fraud.monitoring.business.impl;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.model.TestPosition;
import ru.sber.transport.fraud.monitoring.providers.PositionsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.PositionsGrpcProvider;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


@Slf4j
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка сервиса должностей")
class PositionsServiceImplTest {
    private final PositionsDatabaseProvider positionsDatabaseProvider = mock(PositionsDatabaseProvider.class);
    private final PositionsGrpcProvider positionsGrpcProvider = mock(PositionsGrpcProvider.class);

    private final PositionsServiceImpl positionsService = new PositionsServiceImpl(positionsDatabaseProvider, positionsGrpcProvider);

    @Test
    @DisplayName("Проверка сохранения должности")
    void test_createOrUpdate() {
        final var positionId = UUID.randomUUID();

        final var source = new TestPosition(positionId, "Test Position");

        when(positionsDatabaseProvider.createOrUpdate(source)).thenReturn(source);

        final var result = positionsService.createOrUpdate(source);

        assertThat(result).isEqualTo(source);

        verify(positionsDatabaseProvider).createOrUpdate(source);
    }

    @Test
    @DisplayName("getExistedOrCreate возвращает существующую должность из БД")
    void test_getExistedOrCreate_returnsExistingPosition() {
        final var positionId = UUID.randomUUID();
        final var existingPosition = new TestPosition(positionId, "Existing Position");

        when(positionsDatabaseProvider.get(positionId))
                .thenReturn(existingPosition);

        final var result = positionsService.getExistedOrCreate(positionId);

        assertThat(result).isEqualTo(existingPosition);
        verify(positionsDatabaseProvider).get(positionId);
        verify(positionsGrpcProvider, never()).get(any(UUID.class));
    }

    @Test
    @DisplayName("getExistedOrCreate возвращает должность по gRPC, если не найдена в БД")
    void test_getExistedOrCreate_handlesNullFromDatabase() {
        final var positionId = UUID.randomUUID();
        final var grpcPosition = new TestPosition(positionId, "GRPC Position");

        when(positionsDatabaseProvider.get(positionId))
                .thenReturn(null);
        when(positionsGrpcProvider.get(positionId))
                .thenReturn(grpcPosition);
        when(positionsDatabaseProvider.createOrUpdate(grpcPosition))
                .thenReturn(grpcPosition);

        final var result = positionsService.getExistedOrCreate(positionId);

        assertThat(result).isEqualTo(grpcPosition);
        verify(positionsDatabaseProvider).get(positionId);
        verify(positionsGrpcProvider).get(positionId);
        verify(positionsDatabaseProvider).createOrUpdate(grpcPosition);
    }
}