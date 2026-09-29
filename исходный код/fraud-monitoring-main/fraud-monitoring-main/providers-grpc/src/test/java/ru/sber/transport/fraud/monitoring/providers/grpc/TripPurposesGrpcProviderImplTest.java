package ru.sber.transport.fraud.monitoring.providers.grpc;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.TripPurposesGrpc;
import ru.sber.transport.fraud.monitoring.providers.TripPurposesGrpcProvider;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера целей поездки")
class TripPurposesGrpcProviderImplTest {

    private final TripPurposesGrpc.TripPurposesBlockingStub stub = mock(TripPurposesGrpc.TripPurposesBlockingStub.class);

    private final TripPurposesGrpcProvider tripPurposesProvider = new TripPurposesGrpcProviderImpl(stub);

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        final var id = UUID.randomUUID();
        final var label = Instancio.create(String.class);

        when(stub.one(any())).thenReturn(OrganizationsOuterClass.TripPurpose.newBuilder().setId(id.toString()).setLabel(label).build());

        final var actual = tripPurposesProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getLabel()).isEqualTo(label);
    }

}