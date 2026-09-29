package ru.sber.transport.limits.providers.grpc.department;

import com.google.protobuf.NullValue;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.limits.providers.Departments;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка gRPC провайдера подразделений")
class DepartmentGrpcImplTest {

    private final DepartmentsGrpc.DepartmentsBlockingStub stub = mock(DepartmentsGrpc.DepartmentsBlockingStub.class);

    private final Departments departments = new DepartmentGrpcImpl(stub);

    @Test
    @DisplayName("Проверка получения подразделения")
    void test_get() {
        final var id = UUID.randomUUID();

        final var source = OrganizationsOuterClass.Department.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setParentId(OrganizationsOuterClass.NullableString.newBuilder().setValue(UUID.randomUUID().toString()).build())
                .setName(Instancio.create(String.class))
                .setOrganizationId(UUID.randomUUID().toString())
                .setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setValue(UUID.randomUUID().toString()).build())
                .build();

        when(stub.one(any())).thenReturn(source);

        final var actual = departments.get(id);

        assertSoftly(it -> {
            it.assertThat(actual).isNotNull();
            it.assertThat(actual.id()).isEqualTo(UUID.fromString(source.getId()));
            it.assertThat(actual.parentId()).isEqualTo(UUID.fromString(source.getParentId().getValue()));
        });
    }

    @Test
    @DisplayName("Проверка получения подразделения без родителя")
    void test_get_noParent() {
        final var id = UUID.randomUUID();

        final var source = OrganizationsOuterClass.Department.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setParentId(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .setName(Instancio.create(String.class))
                .setOrganizationId(UUID.randomUUID().toString())
                .setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setValue(UUID.randomUUID().toString()).build())
                .build();

        when(stub.one(any())).thenReturn(source);

        final var actual = departments.get(id);

        assertSoftly(it -> {
            it.assertThat(actual).isNotNull();
            it.assertThat(actual.id()).isEqualTo(UUID.fromString(source.getId()));
            it.assertThat(actual.parentId()).isNull();
        });
    }

}