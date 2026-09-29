package ru.sber.transport.fraud.monitoring.business.impl;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.EmployeesService;
import ru.sber.transport.fraud.monitoring.business.model.TestDepartment;
import ru.sber.transport.fraud.monitoring.model.Department;
import ru.sber.transport.fraud.monitoring.providers.DepartmentsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.DepartmentsGrpcProvider;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


@Slf4j
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка сервиса подразделений")
class DepartmentsServiceImplTest {
    private final DepartmentsDatabaseProvider departmentsDatabaseProvider = mock(DepartmentsDatabaseProvider.class);
    private final DepartmentsGrpcProvider departmentsGrpcProvider = mock(DepartmentsGrpcProvider.class);
    private final EmployeesService employeesService = mock(EmployeesService.class);

    private final DepartmentsServiceImpl departmentsService = new DepartmentsServiceImpl(departmentsDatabaseProvider, departmentsGrpcProvider, employeesService);

    @Test
    @DisplayName("Проверка сохранения подразделения")
    void test_createOrUpdate() {
        final var departmentId = UUID.randomUUID();
        final var headId = UUID.randomUUID();
        final var parentId = UUID.randomUUID();

        final var source = new TestDepartment(
                departmentId,
                headId,
                parentId,
                "Test Department",
                "Test Description"
        );

        when(departmentsDatabaseProvider.createOrUpdate(source)).thenReturn(source);

        final var result = departmentsService.createOrUpdate(source);

        assertThat(result).isEqualTo(source);

        verify(departmentsDatabaseProvider).createOrUpdate(source);

        verify(employeesService, timeout(1000)).getExistedOrCreate(headId);
        verify(departmentsGrpcProvider, timeout(1000)).get(parentId);
    }

    @Test
    @DisplayName("getExistedOrCreate возвращает существующее подразделение из БД")
    void test_getExistedOrCreate_returnsExistingDepartment() {
        final var departmentId = UUID.randomUUID();
        final var existingDepartment = new TestDepartment(
                departmentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Existing Department",
                "Existing Description"
        );

        when(departmentsDatabaseProvider.get(departmentId))
                .thenReturn(existingDepartment);

        final var result = departmentsService.getExistedOrCreate(departmentId);

        assertThat(result).isEqualTo(existingDepartment);
        verify(departmentsDatabaseProvider).get(departmentId);
        verify(departmentsGrpcProvider, never()).get(any(UUID.class));
        verify(departmentsDatabaseProvider, never()).createOrUpdate(any(Department.class));
    }

    @Test
    @DisplayName("getExistedOrCreate возвращает подразделение по gRPC, если не найдена в БД")
    void test_getExistedOrCreate_handlesNullFromDatabase() {
        final var departmentId = UUID.randomUUID();
        final var grpcDepartment = new TestDepartment(
                departmentId,
                null,
                null,
                "GRPC Department",
                "GRPC Description"
        );

        when(departmentsDatabaseProvider.get(departmentId))
                .thenReturn(null);
        when(departmentsGrpcProvider.get(departmentId))
                .thenReturn(grpcDepartment);
        when(departmentsDatabaseProvider.createOrUpdate(grpcDepartment))
                .thenReturn(grpcDepartment);

        final var result = departmentsService.getExistedOrCreate(departmentId);

        assertThat(result).isEqualTo(grpcDepartment);
        verify(departmentsDatabaseProvider).get(departmentId);
        verify(departmentsGrpcProvider).get(departmentId);
        verify(departmentsDatabaseProvider).createOrUpdate(grpcDepartment);
    }
}