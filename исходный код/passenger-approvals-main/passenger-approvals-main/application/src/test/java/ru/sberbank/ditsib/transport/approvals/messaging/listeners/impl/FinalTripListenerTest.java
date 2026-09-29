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
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.instancio.Select.field;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;
import static ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl.ApprovalTestUtils.baseRequestMsg;

@EmbeddedPostgres
@DisplayName("Проверка получения заявок на согласование финальной поездки")
@MockitoBean(types = {JwtDecoder.class})
class FinalTripListenerTest extends KafkaTest {

    @Autowired
    @Qualifier("requestInput")
    private Consumer<Message<RequestMessage>> requestInput;
    
    @Autowired
    private FinalTripApprovalRepository finalTripApprovalRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TripPurposeRepository purposeRepository;
    @Autowired
    private ApprovalJournalRepository approvalJournalRepository;

    private final TestSharedData sharedData = new TestSharedData();
    @AfterEach
    void afterEach() {
        finalTripApprovalRepository.deleteAll();
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
    @DisplayName("Новое согласование для статуса AWAITING_TRIP_APPROVAL")
    void testNewAwaitingTripApproval() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var tripPurpose = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        var message = baseRequestMsg();
        message.setPurposeId(tripPurpose.getId());
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        message.setStatus(TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL.name());
        
        assertThat(finalTripApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(finalTripApprovalRepository.count()).isEqualTo(1);
        
        var actual = finalTripApprovalRepository.findAll().get(0);
        
        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                                                       within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getTransportType()).isEqualTo(message.getTransportType());
        assertThat(actual.getTaxiClass().name()).isEqualTo(message.getTripClass());
        assertThat(actual.getCost()).isEqualTo(message.getExpected().getCost());
        assertThat(actual.getDesiredDate()).isCloseTo(message.getDesiredDate(), within(1, ChronoUnit.SECONDS));
        assertThat(actual.getPurposeId()).isEqualTo(message.getPurposeId());
    
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
    }
    
    @Test
    @DisplayName("Новое согласование для статуса PUBLIC_AWAITING_AFFIRMATIVE")
    void testNewPublicAwaitingAffirmative() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var tripPurpose = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        var message = baseRequestMsg();
        message.setPurposeId(tripPurpose.getId());
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        message.setStatus(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE.name());
        
        assertThat(finalTripApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(finalTripApprovalRepository.count()).isEqualTo(1);
        
        var actual = finalTripApprovalRepository.findAll().get(0);
        
        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                                                       within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getTransportType()).isEqualTo(message.getTransportType());
        assertThat(actual.getTaxiClass().name()).isEqualTo(message.getTripClass());
        assertThat(actual.getCost()).isEqualTo(message.getExpected().getCost());
        assertThat(actual.getDesiredDate()).isCloseTo(message.getDesiredDate(), within(1, ChronoUnit.SECONDS));
        assertThat(actual.getPurposeId()).isEqualTo(message.getPurposeId());
        
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
    }
    
    @Test
    @DisplayName("Новое согласование не создается, так как заявка не в статусе TripRequestListenerImpl.FINAL_APPROVE_STATUSES")
    void testNotCreate() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var message = baseRequestMsg();
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        message.setStatus(TripRequestStatus.TAXI_DRIVER_ARRIVED.name());

        assertThat(finalTripApprovalRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(finalTripApprovalRepository.count()).isZero();
    }
}
