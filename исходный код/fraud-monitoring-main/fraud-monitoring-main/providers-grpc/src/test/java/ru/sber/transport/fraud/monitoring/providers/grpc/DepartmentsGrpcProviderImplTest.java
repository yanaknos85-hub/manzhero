package ru.sber.transport.fraud.monitoring.providers.grpc;

import com.google.protobuf.NullValue;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.fraud.monitoring.providers.DepartmentsGrpcProvider;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера подразделений")
class DepartmentsGrpcProviderImplTest {

    private final DepartmentsGrpc.DepartmentsBlockingStub stub = mock(DepartmentsGrpc.DepartmentsBlockingStub.class);

    private final DepartmentsGrpcProvider departmentsProvider = new DepartmentsGrpcProviderImpl(stub);

    @Test
    @DisplayName("Получение данных")
    void test_get() {
        final var id = UUID.randomUUID();
        final var headId = UUID.randomUUID();

        when(stub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(id.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setValue(headId.toString()).build()).build());

        final var actual = departmentsProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getHeadId()).isEqualTo(headId);
    }

    @Test
    @DisplayName("Получение данных без руководителя")
    void test_get_noHead() {
        final var id = UUID.randomUUID();

        when(stub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(id.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build()).build());

        final var actual = departmentsProvider.get(id);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getHeadId()).isNull();
    }

}