package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.service.DepartmentService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка сервиса подразделений")
class DepartmentServiceImplTest {

    private final DepartmentRepository repository = mock(DepartmentRepository.class);

    private final DepartmentService service = new DepartmentServiceImpl(repository);

    @Test
    @DisplayName("Получение всех")
    void test_getAll() {
        var departments = Instancio.createList(Department.class);

        when(repository.findAll()).thenReturn(departments);

        assertThat(service.getAll()).hasSameElementsAs(departments);
    }

    @Test
    @DisplayName("Получение одного")
    void test_get() {
        var department = Instancio.create(Department.class);
        var organizationId = UUID.randomUUID();

        when(repository.findByIdAndOrganizationId(organizationId, department.getId())).thenReturn(Optional.of(department));

        assertThat(service.get(department.getId(), organizationId)).isPresent();
        assertThat(service.get(department.getId(), organizationId).orElseThrow()).isEqualTo(department);
    }

    @Test
    @DisplayName("Получение по родителю")
    void test_getByParent() {
        var departments = Instancio.createList(Department.class);
        var parentId = UUID.randomUUID();

        when(repository.findByParentId(parentId)).thenReturn(departments);

        assertThat(service.getByParent(parentId)).hasSameElementsAs(departments);
    }

    @Test
    @DisplayName("Получение по коду")
    void test_getByCode() {
        var department = Instancio.ofList(Department.class).size(1).create();
        var department2 = Instancio.ofList(Department.class).size(10).create();
        var code = Instancio.create(String.class);
        var code2 = Instancio.create(String.class);

        when(repository.findByCode(code)).thenReturn(department);
        when(repository.findByCode(code2)).thenReturn(department2);

        assertThat(service.getByCode(code)).isEqualTo(department.getFirst());
        assertThat(service.getByCode(code2)).isNull();
        assertThat(service.getByCode("Any code")).isNull();
    }

}