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
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка получения подразделений")
class DepartmentListenerTest extends OldCommonTest {

    @MockitoBean
    private JwtDecoder decoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private Consumer<Message<DepartmentMessage>> departmentsInput;

    @AfterEach
    void dropRepository() {
        employeeRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @BeforeEach
    void fillRepository() {
        organization1 = Organization.builder().id(UUID.randomUUID()).digitId(1L).build();
        departmentMain = Department.builder()
                .id(UUID.randomUUID())
                .organizationId(organization1.getId())
                .code("1111")
                .departmentName("moscow")
                .build();

        testEmployee1 = Employee.builder()
                .id(userId1)
                .userId(userId1)
                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1)
                .firstName("vasya")
                .lastName("pupkin")
                .departmentId(departmentMain.getId())
                .build();

        testEmployee2 = Employee.builder()
                .id(userId2)
                .userId(userId2)
                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2)
                .firstName("masha")
                .lastName("sidorova")
                .departmentId(departmentMain.getId())
                .build();

        organizationRepository.saveAndFlush(organization1);
        departmentRepository.saveAndFlush(departmentMain);
        employeeRepository.saveAndFlush(testEmployee1);
        employeeRepository.saveAndFlush(testEmployee2);

        testEmployee1.setSupervisorId(testEmployee2.getId());
        employeeRepository.saveAndFlush(testEmployee1);

        departmentMain.setDepartmentHead(testEmployee1);
        departmentRepository.saveAndFlush(departmentMain);
    }

    @Test
    @DisplayName("Новое")
    void handleOrganization_new() {
        var id = UUID.randomUUID();
        String humanReadableId = "DT-0001-1";
        var message = DepartmentMessage.builder()
                .code("5555")
                .departmentHeadId(testEmployee1.getId())
                .departmentName("Seliger")
                .id(id)
                .humanReadableId(humanReadableId)
                .organizationId(organization1.getId())
                .location("Location")
                .deleted(false)
                .parentId(departmentMain.getId())
                .build();

        assertThat(departmentRepository.count()).isEqualTo(1);

        departmentsInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        assertThat(departmentRepository.count()).isEqualTo(2);
        assertThat(departmentRepository.findAll().get(1).getId()).isEqualTo(id);
        assertThat(departmentRepository.findAll().get(1).isActive()).isTrue();
        assertThat(departmentRepository.findAll().get(1).getHumanReadableId()).isEqualTo(humanReadableId);
    }

    @Test
    @DisplayName("Новое. Нет главы подразделения")
    void handleOrganization_new_noDepartmentHead() {
        var id = UUID.randomUUID();
        var departmentHeadId = UUID.randomUUID();
        var message = DepartmentMessage.builder()
                .code("Code")
                .departmentHeadId(departmentHeadId)
                .departmentName("Name")
                .id(id)
                .organizationId(organization1.getId())
                .location("Location")
                .humanReadableId(Instancio.create(String.class))
                .deleted(false)
                .parentId(UUID.randomUUID())
                .build();

        assertThat(departmentRepository.count()).isEqualTo(1);

        departmentsInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        assertThat(departmentRepository.count()).isEqualTo(2);
    }
}