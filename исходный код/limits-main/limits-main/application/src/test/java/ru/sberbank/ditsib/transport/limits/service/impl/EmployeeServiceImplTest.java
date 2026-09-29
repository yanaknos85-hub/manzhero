package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка сервиса сотрудников")
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.lazy-initialization=true")
class EmployeeServiceImplTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private EmployeeService service;

    @Test
    @DisplayName("Получение всех по идентификатору")
    void test_getAllById() {
        var employees = Instancio.createList(Employee.class).parallelStream()
            .map(emp -> repository.save(emp))
            .toList();
        repository.saveAll(employees);

        assertThat(service.getAllById(employees.parallelStream().map(Employee::getId).toList())).hasSameElementsAs(employees);
    }
    @Test
    @DisplayName("Получение по табельнику")
    void test_getByPersonnelNumber() {
        var personnelNumber = Instancio.create(String.class);
        var personnelNumber1 = Instancio.create(String.class);

        var employees = Instancio.ofList(Employee.class).size(1).set(Select.field(Employee::getPersonnelNumber), personnelNumber).create();
        repository.saveAll(employees);

        assertThat(service.getByPersonnelNumber(personnelNumber).getId()).isEqualTo(employees.getFirst().getId());
        assertThat(service.getByPersonnelNumber(personnelNumber1)).isNull();
        assertThat(service.getByPersonnelNumber("Any code")).isNull();
    }

    @Test
    @DisplayName("Получение активного по подразделению")
    void test_findByDepartmentIdAndActive() {
        var departmentId = UUID.randomUUID();
        var employees = Instancio.ofList(Employee.class).set(Select.field(Employee::getDepartmentId), departmentId).set(Select.field(Employee::isActive), true).create();
        employees = repository.saveAll(employees);

        assertThat(service.findByDepartmentIdAndActive(departmentId, true)).hasSameSizeAs(employees);
        assertThat(service.findByDepartmentIdAndActive(departmentId, true).stream().map(Employee::getId).toList()).hasSameSizeAs(employees.stream().map(Employee::getId).toList());
    }

    @Test
    @DisplayName("Получение количества активных по подразделению")
    void test_countByDepartmentIdAndActive() {
        var departmentId = UUID.randomUUID();
        var employees = Instancio.ofList(Employee.class).set(Select.field(Employee::getDepartmentId), departmentId).set(Select.field(Employee::isActive), true).create();
        employees = repository.saveAll(employees);

        assertThat(service.countByDepartmentIdAndActive(departmentId, true)).isEqualTo(employees.size());
    }

    @Test
    @DisplayName("Получение количества сотрудников")
    @Disabled("Тест не стабилен")
    void test_getCount() {
        var active = true;
        var departmentIds = new HashSet<UUID>();

        for (int i = 0; i < 10; i++) {
            var department = departmentRepository.save(Instancio.create(Department.class));

            for (int j = 0; j < i + 1; j++) {
                var employee = Instancio.of(Employee.class)
                    .set(Select.field(Employee::getDepartmentId), department.getId())
                    .create();
                repository.save(employee);
            }

            departmentIds.add(department.getId());
        }

        var actual = service.countByDepartmentsAndActive(departmentIds, active);

        assertThat(actual).hasSize(10);
    }

}