package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.approvals.messaging.ApproveTripRequestMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;
import ru.sberbank.ditsib.transport.approvals.messaging.message.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;
import static ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl.ApprovalTestUtils.baseRequestMsg;

@EmbeddedPostgres
@DisplayName("Проверка получения ответа от сервиса резервирования на поездку")
@MockitoBean(types = {JwtDecoder.class})
class LimitActionResponseListenerImplTest extends KafkaTest {

    @Autowired
    @Qualifier("requestInput")
    private Consumer<Message<RequestMessage>> requestInput;

    @Autowired
    @Qualifier("limitReservationResponse")
    private Consumer<Message<LimitActionResultMessage>> limitReservationResponse;

    @MockitoBean("tripRequestApproveOutput")
    private OutputBridge tripRequestApproveOutput;

    @Autowired
    private TripRequestApprovalRepository tripRequestApprovalRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LimitReservationResponseRepository limitReservationResponseRepository;

    @Autowired
    private TaxiTariffRepository tariffRepository;

    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private ApprovalJournalRepository approvalJournalRepository;

    private final TestSharedData sharedData = new TestSharedData();

    private TaxiTariff tariff;

    @AfterEach
    void afterEach() {
        tariffRepository.deleteAll();
        tripRequestApprovalRepository.deleteAll();
        approvalJournalRepository.deleteAll();
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department
                .setDepartmentHeadId(null)
                .setParentId(null)));
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
        limitReservationResponseRepository.deleteAll();
    }

    @BeforeEach
    void beforeEach() {
        final var organization = organizationRepository.save(Instancio.create(Organization.class));
        tariff = tariffRepository.save(Instancio.of(TaxiTariff.class)
                .set(Select.field(TaxiTariff::getOrganizationId), organization.getId())
                .create());
    }

    @Test
    @DisplayName("Автоаппрув по limit статусу")
    void test_new() {
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
        var message = baseRequestMsg(tariff.getId());
        message.setPurposeId(tripPurpose.getId());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        assertThat(tripRequestApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        var messageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, message.getId())));
        var actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(message.getId());
        assertThat(actualMessage.approved()).isNull();

        var actual = tripRequestApprovalRepository.findAll().getFirst();

        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
        assertThat(actual.getApprovedById()).isNull();

        var limitMessage = LimitActionResultMessage.builder()
                .tripRequestId(message.getId())
                .limitReservationStatus("RESERVED_FROM_EMPLOYEE")
                .build();
        limitReservationResponse.accept(MessageBuilder.withPayload(limitMessage).build());

        actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(actual.getApprovedById()).isEqualTo(message.getPassengerId());

        messageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        final var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tripRequestApproveOutput, times(2)).send(messageCaptor.capture(), headerCaptor.capture());
        final var actualHeaders = headerCaptor.getAllValues().get(1);
        assertThat(actualHeaders).containsEntry(KafkaHeaders.KEY, message.getId());
        actualMessage = messageCaptor.getAllValues().get(1);
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(actual.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }

    @Test
    @DisplayName("Атоаппрув не случился по limit статусу")
    void test_new_not_auto_approve() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var message = baseRequestMsg(tariff.getId());
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        message.setPurposeId(tripPurpose.getId());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        assertThat(tripRequestApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        final var messageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, message.getId())));
        var actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(message.getId());
        assertThat(actualMessage.approved()).isNull();

        var actual = tripRequestApprovalRepository.findAll().getFirst();

        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
        assertThat(actual.getApprovedById()).isNull();

        var limitMessage = LimitActionResultMessage.builder()
                .tripRequestId(message.getId())
                .limitReservationStatus(
                        "RESERVED_FROM_DEPARTMENT")
                .build();
        limitReservationResponse.accept(MessageBuilder.withPayload(limitMessage).build());

        actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
        assertThat(actual.getApprovedById()).isNull();

    }

    @Test
    @DisplayName(
            "После прихода limit автоаппрув не происходит, так как нет request. После прихода request делается автоаппрув ")
    void test_limit_request() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        assertThat(tripRequestApprovalRepository.count()).isZero();
        assertThat(limitReservationResponseRepository.count()).isZero();

        // create limit reserve result message
        final UUID tripRequestId = UUID.randomUUID();
        var limitMessage = LimitActionResultMessage.builder()
                .tripRequestId(tripRequestId)
                .limitReservationStatus(
                        "RESERVED_FROM_EMPLOYEE")
                .build();
        limitReservationResponse.accept(MessageBuilder.withPayload(limitMessage).build());

        assertThat(tripRequestApprovalRepository.count()).isZero();
        assertThat(limitReservationResponseRepository.count()).isEqualTo(1);

        // create new request message in AWAITING_APPROVAL status
        var message = baseRequestMsg(tariff.getId());
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        message.setPurposeId(tripPurpose.getId());
        message.setId(tripRequestId);
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        assertThat(message.getStatus()).isEqualTo(TripRequestStatus.TAXI_AWAITING_APPROVAL.name());
        assertThat(tripRequestApprovalRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(message).build());

        // after process message check that created request with status APPROVED
        TripRequestApproval actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getActionId()).isEqualTo(tripRequestId);
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(actual.getApprovedById()).isEqualTo(message.getPassengerId());
        assertThat(limitReservationResponseRepository.count()).isEqualTo(1);

        final var messageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, tripRequestId)));
        var actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(actual.getActionId());
        assertThat(actualMessage.approved()).isTrue();

    }

}
