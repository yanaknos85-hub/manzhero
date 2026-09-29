package ru.sber.transport.fraud.monitoring.providers.grpc;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.PositionsGrpc;
import ru.sber.transport.fraud.monitoring.providers.PositionsGrpcProvider;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера должностей")
class PositionsProviderGrpcImplTest {

    private final PositionsGrpc.PositionsBlockingStub stub = mock(PositionsGrpc.PositionsBlockingStub.class);

    private final PositionsGrpcProvider positionsProvider = new PositionsGrpcProviderImpl(stub);

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        final var id = UUID.randomUUID();
        final var name = Instancio.create(String.class);

        when(stub.one(any())).thenReturn(OrganizationsOuterClass.Position.newBuilder().setId(id.toString()).setName(name).build());

        final var actual = positionsProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getName()).isEqualTo(name);
    }
}