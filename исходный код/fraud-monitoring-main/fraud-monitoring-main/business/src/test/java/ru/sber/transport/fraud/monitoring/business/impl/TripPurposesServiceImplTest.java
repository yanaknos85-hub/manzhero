package ru.sber.transport.fraud.monitoring.business.impl;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.model.TestTripPurpose;
import ru.sber.transport.fraud.monitoring.providers.TripPurposesDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.TripPurposesGrpcProvider;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


@Slf4j
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка сервиса целей поездки")
class TripPurposesServiceImplTest {
    private final TripPurposesDatabaseProvider tripPurposesDatabaseProvider = mock(TripPurposesDatabaseProvider.class);
    private final TripPurposesGrpcProvider tripPurposesGrpcProvider = mock(TripPurposesGrpcProvider.class);

    private final TripPurposesServiceImpl tripPurposesService = new TripPurposesServiceImpl(tripPurposesDatabaseProvider, tripPurposesGrpcProvider);

    @Test
    @DisplayName("Проверка сохранения цели поездки")
    void test_createOrUpdate() {
        final var purposeId = UUID.randomUUID();

        final var source = new TestTripPurpose(purposeId, "Test Purpose");

        when(tripPurposesDatabaseProvider.createOrUpdate(source)).thenReturn(source);

        final var result = tripPurposesService.createOrUpdate(source);

        assertThat(result).isEqualTo(source);

        verify(tripPurposesDatabaseProvider).createOrUpdate(source);
    }

    @Test
    @DisplayName("getExistedOrCreate возвращает существующую цель поездки из БД")
    void test_getExistedOrCreate_returnsExistingTripPurpose() {
        final var purposeId = UUID.randomUUID();
        final var existingTripPurpose = new TestTripPurpose(purposeId, "Existing Purpose");

        when(tripPurposesDatabaseProvider.get(purposeId))
                .thenReturn(existingTripPurpose);

        final var result = tripPurposesService.getExistedOrCreate(purposeId);

        assertThat(result).isEqualTo(existingTripPurpose);
        verify(tripPurposesDatabaseProvider).get(purposeId);
        verify(tripPurposesGrpcProvider, never()).get(any(UUID.class));
    }

    @Test
    @DisplayName("getExistedOrCreate возвращает цель поездки по gRPC, если не найдена в БД")
    void test_getExistedOrCreate_handlesNullFromDatabase() {
        final var purposeId = UUID.randomUUID();
        final var grpcTripPurpose = new TestTripPurpose(purposeId, "GRPC Purpose");

        when(tripPurposesDatabaseProvider.get(purposeId))
                .thenReturn(null);
        when(tripPurposesGrpcProvider.get(purposeId))
                .thenReturn(grpcTripPurpose);
        when(tripPurposesDatabaseProvider.createOrUpdate(grpcTripPurpose))
                .thenReturn(grpcTripPurpose);

        final var result = tripPurposesService.getExistedOrCreate(purposeId);

        assertThat(result).isEqualTo(grpcTripPurpose);
        verify(tripPurposesDatabaseProvider).get(purposeId);
        verify(tripPurposesGrpcProvider).get(purposeId);
        verify(tripPurposesDatabaseProvider).createOrUpdate(grpcTripPurpose);
    }
}