package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.Position;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;
import ru.sberbank.ditsib.transport.approvals.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.approvals.messaging.message.UpdateTripRequestMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.instancio.Select.field;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;
import static ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl.ApprovalTestUtils.baseRequestMsg;

@EmbeddedPostgres
@DisplayName("Проверка получения заявок на изменение маршрута")
@MockitoBean(types = {JwtDecoder.class})
class UpdateTripRequestListenerImplTest extends KafkaTest {

    @Autowired
    @Qualifier("updateTripRequestInput")
    private Consumer<Message<UpdateTripRequestMessage>> updateTripRequestInput;

    @Autowired
    private UpdateTripRequestApprovalRepository updateTripRequestApprovalRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private ApprovalJournalRepository approvalJournalRepository;

    private final TestSharedData sharedData = new TestSharedData();

    @AfterEach
    void afterEach() {
        updateTripRequestApprovalRepository.deleteAll();
        approvalJournalRepository.deleteAll();
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department
                .setDepartmentHeadId(null)
                .setParentId(null)));
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    @DisplayName("Новая")
    void testNew() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setFraudData(Collections.emptyList());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());

        var message = new UpdateTripRequestMessage();
        message.setId(UUID.randomUUID());
        message.setRequest(requestMessage);

        assertThat(updateTripRequestApprovalRepository.count()).isZero();

        updateTripRequestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(updateTripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = updateTripRequestApprovalRepository.findAll().get(0);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUpdateId()).isEqualTo(message.getId());
        assertThat(actual.getActionId()).isEqualTo(message.getRequest().getId());
        assertThat(actual.getActorId()).isEqualTo(message.getRequest().getPassengerId());
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getRequest().getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
        assertThat(actual.getApprovedById()).isNull();
        assertThat(actual.getTransportType()).isEqualTo(message.getRequest().getTransportType());
        assertThat(actual.getTaxiClass().name()).isEqualTo(message.getRequest().getTripClass());
        assertThat(actual.getCost()).isEqualTo(message.getRequest().getExpected().getCost());
        assertThat(actual.getDesiredDate()).isCloseTo(message.getRequest().getDesiredDate(), within(1, ChronoUnit.SECONDS));
        assertThat(actual.getPurposeId()).isEqualTo(message.getRequest().getPurposeId());
    }

    @Test
    @DisplayName("Отмена")
    void testDelete() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setFraudData(Collections.emptyList());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        var message = new UpdateTripRequestMessage();
        message.setId(UUID.randomUUID());
        message.setRequest(requestMessage);

        assertThat(updateTripRequestApprovalRepository.count()).isZero();
        updateTripRequestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(updateTripRequestApprovalRepository.count()).isEqualTo(1);
        var actual = updateTripRequestApprovalRepository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);

        message.setDeleted(true);
        updateTripRequestInput.accept(MessageBuilder.withPayload(message).build());
        actual = updateTripRequestApprovalRepository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.CANCELLED);

    }

    @Test
    @DisplayName("Отмена согласованной")
    void test_deleteApproved() {
        var org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setFraudData(Collections.emptyList());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());

        var message = new UpdateTripRequestMessage();
        message.setId(UUID.randomUUID());
        message.setRequest(requestMessage);

        assertThat(updateTripRequestApprovalRepository.count()).isZero();
        updateTripRequestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(updateTripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = updateTripRequestApprovalRepository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
        actual.setStatus(Status.APPROVED);
        updateTripRequestApprovalRepository.save(actual);
        actual = updateTripRequestApprovalRepository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);

        message.setDeleted(true);
        updateTripRequestInput.accept(MessageBuilder.withPayload(message).build());
        actual = updateTripRequestApprovalRepository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);

    }

}
