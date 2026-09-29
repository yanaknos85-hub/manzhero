package ru.sber.transport.fraud.monitoring.providers.grpc;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.fraud.monitoring.providers.OrganizationsGrpcProvider;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера организаций (GRPC)")
class OrganizationsGrpcProviderImplTest {

    private final OrganizationsGrpc.OrganizationsBlockingStub stub = mock(OrganizationsGrpc.OrganizationsBlockingStub.class);

    private final OrganizationsGrpcProvider organizationsGrpcProvider = new OrganizationGrpcProviderImpl(stub);

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        final var id = UUID.randomUUID();
        final var digitId = Instancio.create(Integer.class);

        when(stub.one(any())).thenReturn(OrganizationsOuterClass.Organization.newBuilder().setId(id.toString()).setDigitId(digitId).build());

        final var actual = organizationsGrpcProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getDigitId()).isEqualTo((long) digitId);
    }

}