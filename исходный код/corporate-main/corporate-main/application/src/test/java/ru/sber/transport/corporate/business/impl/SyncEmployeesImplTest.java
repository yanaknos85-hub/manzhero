package ru.sber.transport.corporate.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.corporate.business.Employees;
import ru.sber.transport.corporate.business.model.*;
import ru.sber.transport.corporate.business.providers.*;
import ru.sber.transport.corporate.web.grpc.client.SyncClient;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка работы с сотрудниками")
class SyncEmployeesImplTest {

    private final Provider<Employee, EmployeeFilter> provider = mock(Provider.class);

    private final DepartmentProvider departmentProvider = mock(ru.sber.transport.corporate.business.providers.DepartmentProvider.class);

    private final Provider<Position, PositionFilter> positionProvider = mock(Provider.class);

    private final AttributeProvider attributeProvider = mock(AttributeProvider.class);

    private final ApprovalsProvider approvalsProvider = mock(ApprovalsProvider.class);

    private final SyncClient syncClient = mock(SyncClient.class);

    private final EmployeeOrganizationFunction func = mock(EmployeeOrganizationFunction.class);

    private final Employees business = new EmployeesImpl(provider, positionProvider, departmentProvider, attributeProvider, approvalsProvider, syncClient, func);

    @Test
    @DisplayName("Получение данных сотрудника. Не найден")
    void test_get_employee_not_found() {
        var employeeId = UUID.randomUUID();

        assertThatThrownBy(() -> business.get(employeeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityId", employeeId)
                .hasFieldOrPropertyWithValue("entityName", "Employee");
    }

    @Test
    @DisplayName("Получение данных сотрудника. Нет должности")
    void test_get_employee_no_position() {
        var employeeId = UUID.randomUUID();

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), employeeId)
                .create();

        when(provider.get(employeeId)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> business.get(employeeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityId", employee.getPositionId())
                .hasFieldOrPropertyWithValue("entityName", "Position");
    }

    @Test
    @DisplayName("Получение данных сотрудника. Запрос. Нет должности")
    void test_get_employee_request_no_position() {
        var employeeId = UUID.randomUUID();

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), employeeId)
                .create();

        when(provider.get(employeeId)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> business.get(employeeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityId", employee.getPositionId())
                .hasFieldOrPropertyWithValue("entityName", "Position");
    }

    @Test
    @DisplayName("Получение данных сотрудника")
    void test_get_employee() {
        var employeeId = UUID.randomUUID();

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), employeeId)
                .create();
        var position = Instancio.of(Position.class)
                .set(Select.field(Position::getId), employee.getPositionId())
                .create();

        when(provider.get(employeeId)).thenReturn(Optional.of(employee));
        when(positionProvider.get(employee.getPositionId())).thenReturn(Optional.of(position));

        var actual = business.get(employeeId);

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(employee.getId());
            it.assertThat(actual.getFirstName()).isEqualTo(employee.getFirstName());
            it.assertThat(actual.getLastName()).isEqualTo(employee.getLastName());
            it.assertThat(actual.getPatronymic()).isEqualTo(employee.getPatronymic());
            it.assertThat(actual.getPersonnelNumber()).isEqualTo(employee.getPersonnelNumber());
            it.assertThat(actual.getPositionId()).isEqualTo(employee.getPositionId());
            it.assertThat(actual.getPhone()).isEqualTo(employee.getPhone());
            it.assertThat(actual.getEmail()).isEqualTo(employee.getEmail());
            it.assertThat(actual.getSupervisorId()).isEqualTo(employee.getSupervisorId());
            it.assertThat(actual.getDepartmentId()).isEqualTo(employee.getDepartmentId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(employee.getOrganizationId());
            it.assertThat(actual.getPositionName()).isEqualTo(position.getName());
            it.assertThat(actual.getStatus().name()).isEqualTo(employee.getStatus().name());
            it.assertThat(actual.getAttributes()).hasSameSizeAs(employee.getAttributes());
            it.assertThat(actual.getHumanReadableId()).isEqualTo(employee.getHumanReadableId());
            it.assertThat(actual.getApprovals()).isEqualTo(employee.getApprovals());
            it.assertThat(actual.isDepartmentHead()).isEqualTo(employee.isDepartmentHead());
            it.assertThat(actual.getManagedDepartments()).hasSameElementsAs(employee.getManagedDepartments());
            it.assertThat(actual.isConsent()).isEqualTo(employee.isConsent());
            it.assertThat(actual.getStructureType().name()).isEqualTo(employee.getStructureType().name());
        });
    }

    @Test
    @DisplayName("Получение данных сотрудника. Есть подчиненные")
    void test_get_employee_managed() {
        var employeeId = UUID.randomUUID();

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), employeeId)
                .create();
        var position = Instancio.of(Position.class)
                .set(Select.field(Position::getId), employee.getPositionId())
                .create();
        var departments = Instancio.createList(UUID.class);

        when(provider.get(employeeId)).thenReturn(Optional.of(employee));
        when(positionProvider.get(employee.getPositionId())).thenReturn(Optional.of(position));
        when(departmentProvider.findOfHead(employeeId)).thenReturn(departments);

        var actual = business.get(employeeId);

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(employee.getId());
            it.assertThat(actual.getFirstName()).isEqualTo(employee.getFirstName());
            it.assertThat(actual.getLastName()).isEqualTo(employee.getLastName());
            it.assertThat(actual.getPatronymic()).isEqualTo(employee.getPatronymic());
            it.assertThat(actual.getPersonnelNumber()).isEqualTo(employee.getPersonnelNumber());
            it.assertThat(actual.getPositionId()).isEqualTo(employee.getPositionId());
            it.assertThat(actual.getPhone()).isEqualTo(employee.getPhone());
            it.assertThat(actual.getEmail()).isEqualTo(employee.getEmail());
            it.assertThat(actual.getSupervisorId()).isEqualTo(employee.getSupervisorId());
            it.assertThat(actual.getDepartmentId()).isEqualTo(employee.getDepartmentId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(employee.getOrganizationId());
            it.assertThat(actual.getPositionName()).isEqualTo(position.getName());
            it.assertThat(actual.getStatus().name()).isEqualTo(employee.getStatus().name());
            it.assertThat(actual.getAttributes()).hasSameSizeAs(employee.getAttributes());
            it.assertThat(actual.getHumanReadableId()).isEqualTo(employee.getHumanReadableId());
            it.assertThat(actual.getApprovals()).isEqualTo(employee.getApprovals());
            it.assertThat(actual.isDepartmentHead()).isTrue();
            it.assertThat(actual.getManagedDepartments()).hasSameElementsAs(departments);
            it.assertThat(actual.isConsent()).isEqualTo(employee.isConsent());
            it.assertThat(actual.getStructureType().name()).isEqualTo(employee.getStructureType().name());
        });
    }

    @Test
    @DisplayName("Получение данных сотрудника. С атрибутами и согласованиями")
    void test_get_employee_attributes_approvals() {
        var employeeId = UUID.randomUUID();

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), employeeId)
                .create();
        var position = Instancio.of(Position.class)
                .set(Select.field(Position::getId), employee.getPositionId())
                .create();
        var attributes = Instancio.createList(Attribute.class);
        var approvals = Instancio.create(Integer.class);

        when(provider.get(employeeId)).thenReturn(Optional.of(employee));
        when(positionProvider.get(employee.getPositionId())).thenReturn(Optional.of(position));
        when(departmentProvider.findOfHead(employeeId)).thenReturn(List.of());
        when(attributeProvider.findOfEmployee(employeeId)).thenReturn(attributes);
        when(approvalsProvider.countOfEmployee(employeeId)).thenReturn(approvals);

        var actual = business.get(employeeId);

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(employee.getId());
            it.assertThat(actual.getFirstName()).isEqualTo(employee.getFirstName());
            it.assertThat(actual.getLastName()).isEqualTo(employee.getLastName());
            it.assertThat(actual.getPatronymic()).isEqualTo(employee.getPatronymic());
            it.assertThat(actual.getPersonnelNumber()).isEqualTo(employee.getPersonnelNumber());
            it.assertThat(actual.getPositionId()).isEqualTo(employee.getPositionId());
            it.assertThat(actual.getPhone()).isEqualTo(employee.getPhone());
            it.assertThat(actual.getEmail()).isEqualTo(employee.getEmail());
            it.assertThat(actual.getSupervisorId()).isEqualTo(employee.getSupervisorId());
            it.assertThat(actual.getDepartmentId()).isEqualTo(employee.getDepartmentId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(employee.getOrganizationId());
            it.assertThat(actual.getPositionName()).isEqualTo(position.getName());
            it.assertThat(actual.getStatus().name()).isEqualTo(employee.getStatus().name());
            it.assertThat(actual.getAttributes()).hasSameElementsAs(attributes);
            it.assertThat(actual.getHumanReadableId()).isEqualTo(employee.getHumanReadableId());
            it.assertThat(actual.getApprovals()).isEqualTo(approvals);
            it.assertThat(actual.isDepartmentHead()).isFalse();
            it.assertThat(actual.getManagedDepartments()).isEmpty();
            it.assertThat(actual.isConsent()).isEqualTo(employee.isConsent());
            it.assertThat(actual.getStructureType().name()).isEqualTo(employee.getStructureType().name());
        });
    }

}