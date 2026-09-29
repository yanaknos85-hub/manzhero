package ru.sber.transport.limits.providers.grpc.employee;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.limits.providers.Employees;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка gRPC провайдера сотрудников")
class EmployeeGrpcImplTest {

    private final EmployeesGrpc.EmployeesBlockingStub stub = mock(EmployeesGrpc.EmployeesBlockingStub.class);

    private final Employees employees = new EmployeeGrpcImpl(stub);

    @Test
    @DisplayName("Проверка получения сотрудника")
    void test_get() {
        final var id = UUID.randomUUID();

        final var source = OrganizationsOuterClass.Employee.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setDepartmentId(UUID.randomUUID().toString())
                .setPositionId(UUID.randomUUID().toString())
                .setOrganizationId(UUID.randomUUID().toString())
                .setCostCenter(OrganizationsOuterClass.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                .setLastName(Instancio.create(String.class))
                .setFirstName(Instancio.create(String.class))
                .setPatronymic(OrganizationsOuterClass.NullableString.newBuilder().setValue(Instancio.create(String.class)).build())
                .setConsent(Instancio.create(Boolean.class))
                .build();

        when(stub.one(any())).thenReturn(source);

        final var actual = employees.get(id);

        assertSoftly(it -> {
            it.assertThat(actual).isNotNull();
            it.assertThat(actual.id()).isEqualTo(UUID.fromString(source.getId()));
            it.assertThat(actual.departmentId()).isEqualTo(UUID.fromString(source.getDepartmentId()));
        });
    }

}