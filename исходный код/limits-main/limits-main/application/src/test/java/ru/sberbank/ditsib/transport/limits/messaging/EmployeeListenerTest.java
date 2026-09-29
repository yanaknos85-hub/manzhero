package ru.sberbank.ditsib.transport.limits.messaging;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.OldCommonTest;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@DisplayName("Проверка получения сотрудников")
class EmployeeListenerTest extends OldCommonTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private Consumer<Message<EmployeeMessage>> employeesInput;

    private Department department;

    private UUID organizationId;

    @AfterEach
    void dropRepository() {
        employeeRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @BeforeEach
    void fillRepository() {
        organizationId = UUID.randomUUID();
        var organization = organizationRepository.save(Organization.builder()
                .id(organizationId)
                .digitId(1L)
                .build());

        var headDepartmentId = UUID.randomUUID();
        department = departmentRepository.save(Department.builder()
                .id(headDepartmentId)
                .organizationId(organization.getId())
                .code("Code")
                .departmentName("Name")
                .build());
        employeeRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Новое")
    void handleOrganization_new() {
        String humanReadableId = "US-0001-1";
        var id = UUID.randomUUID();
        var message = EmployeeMessage.builder()
                .id(id)
                .userId(id)
                .humanReadableId(humanReadableId)
                .departmentId(department.getId())
                .organizationId(organizationId)
                .positionId(UUID.randomUUID())
                .personnelNumber(Instancio.create(String.class))
                .consent(Instancio.create(Boolean.class))
                .deleted(false)
                .firstName("First")
                .employeeType(Instancio.create(String.class))
                .lastName("Last")
                .attributes(Set.of())
                .build();

        assertThat(employeeRepository.count()).isZero();

        employeesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        assertThat(employeeRepository.count()).isEqualTo(1);
        assertThat(employeeRepository.findAll().getFirst().getId()).isEqualTo(id);
        assertThat(employeeRepository.findAll().getFirst().getDepartmentId()).isEqualTo(department.getId());
        assertThat(employeeRepository.findAll().getFirst().getOrganizationId()).isEqualTo(organizationId);
        assertThat(employeeRepository.findAll().getFirst().isActive()).isTrue();
        assertThat(employeeRepository.findAll().getFirst().getHumanReadableId()).isEqualTo(humanReadableId);
    }

    @Test
    @DisplayName("Новое. Нет подразделения")
    void handleOrganization_new_noDepartment() {
        var id = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var message = EmployeeMessage.builder()
                .id(id)
                .userId(id)
                .organizationId(organizationId)
                .humanReadableId(Instancio.create(String.class))
                .personnelNumber(Instancio.create(String.class))
                .consent(Instancio.create(Boolean.class))
                .deleted(false)
                .positionId(UUID.randomUUID())
                .firstName("First")
                .employeeType(Instancio.create(String.class))
                .departmentId(departmentId)
                .lastName("Last")
                .attributes(Set.of())
                .build();

        assertThat(employeeRepository.count()).isZero();
        employeesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
        assertThat(employeeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        employeeRepository.deleteAllInBatch();

        var id = UUID.randomUUID();
        employeeRepository.save(Employee.builder().id(id)
                .departmentId(department.getId())
                .humanReadableId("US-0001-1")
                .firstName("masha")
                .lastName("pupkina")
                .build());
        var message = EmployeeMessage.builder()
                .id(id)
                .userId(id)
                .departmentId(department.getId())
                .organizationId(department.getOrganizationId())
                .positionId(UUID.randomUUID())
                .personnelNumber(Instancio.create(String.class))
                .firstName("First")
                .lastName("Last")
                .humanReadableId(Instancio.create(String.class))
                .employeeType(Instancio.create(String.class))
                .deleted(true)
                .consent(true)
                .attributes(Set.of())
                .build();

        assertThat(employeeRepository.count()).isEqualTo(1);

        employeesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        assertEquals(0, employeeRepository.findByActive(true).size());
    }
}