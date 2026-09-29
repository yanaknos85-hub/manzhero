package ru.sberbank.ditsib.corpclient.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка сервиса подразделений")
@SpringBootTest(properties = { "spring.jpa.show-sql=true" })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Import(ObjectMapper.class)
@Transactional
@ActiveProfiles("test")
public class DepartmentServiceImplTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Test
    @DisplayName("Проверка существования по имени")
    void test_existsByName() {
        var organization = Instancio.of(Organization.class)
                .ignore(Select.field(Organization::getDepartments))
                .ignore(Select.field(Organization::getEmployees))
                .ignore(Select.field(Organization::getPositions))
                .ignore(Select.field(Organization::getContacts))
                .ignore(Select.field(Organization::getOrganizationGroup))
                .ignore(Select.field(Organization::getTripPurposes))
                .ignore(Select.field(Organization::getCargoTypes))
                .ignore(Select.field(Organization::getId))
                .create();
        organization = organizationRepository.save(organization);

        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganization), organization)
                .ignore(Select.field(Department::getEmployees))
                .ignore(Select.field(Department::getChildren))
                .ignore(Select.field(Department::getParent))
                .ignore(Select.field(Department::getHead))
                .ignore(Select.field(Department::getId))
                .create();

        department = departmentRepository.save(department);

        assertThat(departmentService.existsName(organization.getId(), department.getName(), null)).isTrue();
        assertThat(departmentService.existsName(organization.getId(), department.getName(), department.getId())).isFalse();
        assertThat(departmentService.existsName(organization.getId(), "Новое имя", null)).isFalse();
    }

    @Test
    @DisplayName("Проверка существования по коду")
    void test_existsByCode() {
        var organization = Instancio.of(Organization.class)
                .ignore(Select.field(Organization::getDepartments))
                .ignore(Select.field(Organization::getEmployees))
                .ignore(Select.field(Organization::getPositions))
                .ignore(Select.field(Organization::getContacts))
                .ignore(Select.field(Organization::getOrganizationGroup))
                .ignore(Select.field(Organization::getTripPurposes))
                .ignore(Select.field(Organization::getCargoTypes))
                .ignore(Select.field(Organization::getId))
                .create();
        organization = organizationRepository.save(organization);

        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganization), organization)
                .ignore(Select.field(Department::getEmployees))
                .ignore(Select.field(Department::getChildren))
                .ignore(Select.field(Department::getParent))
                .ignore(Select.field(Department::getHead))
                .ignore(Select.field(Department::getId))
                .ignore(Select.field(Organization::getCargoTypes))
                .create();

        department = departmentRepository.save(department);

        assertThat(departmentService.existsCode(organization.getId(), department.getCode(), null)).isTrue();
        assertThat(departmentService.existsCode(organization.getId(), department.getCode(), department.getId())).isFalse();
        assertThat(departmentService.existsCode(organization.getId(), "Новое имя", null)).isFalse();
    }
}
