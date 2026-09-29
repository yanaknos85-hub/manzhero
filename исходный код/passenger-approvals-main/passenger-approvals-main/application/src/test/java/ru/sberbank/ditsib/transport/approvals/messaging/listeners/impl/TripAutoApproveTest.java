package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;
import static ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl.ApprovalTestUtils.baseRequestMsg;

@EmbeddedPostgres
@DisplayName("Проверка автоаппрува")
@MockitoBean(types = {JwtDecoder.class})
@SpringBootTest
class TripAutoApproveTest extends KafkaTest {
    @Autowired
    @Qualifier("requestInput")
    private Consumer<Message<RequestMessage>> requestInput;
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
    private TaxiTariffRepository tariffRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private ApprovalJournalRepository approvalJournalRepository;
    
    private final TestSharedData sharedData = new TestSharedData();
    @AfterEach
    void afterEach() {
        tariffRepository.deleteAll();
        tripRequestApprovalRepository.deleteAll();
        approvalJournalRepository.deleteAll();
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department.setDepartmentHeadId(null)));
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Автосогласование сразу после прихода сотрудника, так как в должности указано автосогласование")
    void testCreateEmployeeAndAutoApproveBySelfAutoApprove() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        final var tariff = tariffRepository.save(Instancio.of(TaxiTariff.class).set(Select.field(TaxiTariff::getOrganizationId), organization.getId()).create());
        //Create position with self approved
        assertThat(positionRepository.count()).isEqualTo(1);
        var positionMessage = PositionMessage.builder()
                .id(UUID.randomUUID())
                .positionName("Test Position")
                .organizationId(ORGANIZATION_1_ID)
                .selfApproved(true)
                .build();
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        produceMessage("service.organization.position", positionMessage);
        assertThat(positionRepository.count()).isEqualTo(2);

        //Create request => not autoapprove because emplyee not exists
        assertThat(tariffRepository.count()).isEqualTo(1);
        assertThat(tripRequestApprovalRepository.count()).isZero();
        var requestMessage = baseRequestMsg(tariff.getId());
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, requestMessage.getId())));
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(requestMessage.getId());
        assertThat(actualMessage.approved()).isNull();
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        var tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();

        //Create employee => autoapprove because position self approved
        var department = departmentRepository.save(sharedData.createDepartment(UUID.randomUUID(), organization.getId()));
        var employeeMessage = EmployeeMessage.builder()
                .id(requestMessage.getPassengerId())
                .positionId(positionMessage.getId())
                .departmentId(department.getId())
                .organizationId(ORGANIZATION_1_ID)
                .firstName("Test first name")
                .lastName("Test last name")
                .humanReadableId(UUID.randomUUID().toString())
                .personnelNumber(UUID.randomUUID().toString())
                .build();
        produceMessage("service.organization.employee", employeeMessage);
        assertThat(employeeRepository.count()).isEqualTo(1);
        tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(tripApproval.getApprovedById()).isEqualTo(requestMessage.getPassengerId());
        actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        final var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tripRequestApproveOutput, times(2)).send(actualMessageCaptor.capture(), headerCaptor.capture());
        actualMessage = actualMessageCaptor.getAllValues().get(1);
        final var actualHeaders = headerCaptor.getAllValues().get(1);
        assertThat(actualHeaders).containsEntry(KafkaHeaders.KEY, requestMessage.getId());
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripApproval.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }

    @Test
    @DisplayName("Автосогласования сразу после прихода сотрудника не происходит, так как в должности не указано автосогласование")
    void testCreateEmployeeAndNotAutoApproveBySelfAutoApprove() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        //Create position with self approved
        assertThat(positionRepository.count()).isEqualTo(1);
        var positionMessage = PositionMessage.builder()
                .id(UUID.randomUUID())
                .positionName("Test Position")
                .organizationId(ORGANIZATION_1_ID)
                .selfApproved(false)
                .build();
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        produceMessage("service.organization.position", positionMessage);
        assertThat(positionRepository.count()).isEqualTo(2);

        //Create request => not autoapprove because emplyee not exists
        assertThat(tripRequestApprovalRepository.count()).isZero();
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        var tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();

        //Create employee => autoapprove because position self approved
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getId), UUID.randomUUID())
                .set(field(Department::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        var employeeMessage = EmployeeMessage.builder()
                .id(UUID.randomUUID())
                .departmentId(department.getId())
                .firstName("Test first name")
                .lastName("Test last name")
                .humanReadableId(UUID.randomUUID().toString())
                .personnelNumber(UUID.randomUUID().toString())
                .id(requestMessage.getPassengerId())
                .positionId(positionMessage.getId())
                .build();
        produceMessage("service.organization.employee", employeeMessage);
        assertThat(employeeRepository.count()).isEqualTo(1);
        tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();
    }

    @Test
    @DisplayName("Автосогласование после изменения должности на self approved сразу после прихода сотрудника не происходит, так как в должности не указано автосогласование")
    void testAutoApproveBySelfAutoApproveAfterChangePosition() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        //Create position with self approved
        assertThat(positionRepository.count()).isZero();
        var positionMessage = PositionMessage.builder()
                                             .id(UUID.randomUUID())
                                             .positionName("Test Position")
                                             .organizationId(ORGANIZATION_1_ID)
            .selfApproved(false)
            .build();
        produceMessage("service.organization.position", positionMessage);
        assertThat(positionRepository.count()).isEqualTo(1);
    
        var employee1 = employeeRepository.save(
                Employee.builder()
                        .id(EMPLOYEE1_ID)
                        .departmentId(DEPARTMENT1_ID)
                        .humanReadableId("EM-" + Instancio.create(Integer.class) + "-" + Instancio.create(Integer.class))
                        .firstName("First " + Instancio.create(Integer.class))
                        .lastName("Last " + Instancio.create(Integer.class))
                        .personnelNumber(String.valueOf(Instancio.create(Integer.class)))
                        .positionId(positionMessage.getId())
                        .userId(UUID.randomUUID())
                        .build());
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        //Create request => not autoapprove because employee not exists
        assertThat(tripRequestApprovalRepository.count()).isZero();
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        var tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();

        // Change self approved position
        final Position position = positionRepository.findAll().getFirst();
        positionMessage = PositionMessage.builder()
                .id(UUID.randomUUID())
                .positionName("Test Position")
                .organizationId(ORGANIZATION_1_ID)
                .id(position.getId())
                .selfApproved(true)
                .build();
        produceMessage("service.organization.position", positionMessage);
        assertThat(positionRepository.count()).isEqualTo(1);
        tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(tripApproval.getApprovedById()).isEqualTo(requestMessage.getPassengerId());
    }

    @Test
    @DisplayName("Не происходит автосогласование, пока не придет должность с автосогласованием")
    void testCreateEmployeeAndPositionAndAutoApproveBySelfAutoApprove() {
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
        //Create request => not autoapprove because emplyee not exists
        assertThat(tripRequestApprovalRepository.count()).isZero();
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        var tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();

        // Change self approved position
        final var employee = employeeRepository.findAll().getFirst();
        var positionMessage = PositionMessage.builder()
                .id(UUID.randomUUID())
                .positionName("Test Position")
                .organizationId(ORGANIZATION_1_ID)
                .id(employee.getPositionId())
                .selfApproved(true)
                .build();
        produceMessage("service.organization.position", positionMessage);
        assertThat(positionRepository.count()).isEqualTo(1);
        tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(tripApproval.getApprovedById()).isEqualTo(requestMessage.getPassengerId());
    }

    @Test
    @DisplayName("Не происходит автосогласования, так как actor id не совпадает с head id")
    void testNotAutoApproveByDepartmentHead() {
        organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        var department = departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, ORGANIZATION_1_ID));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var employee2 = employeeRepository.save(sharedData.createEmployee(UUID.randomUUID(), DEPARTMENT1_ID, position.getId()));
        departmentRepository.save(department.setDepartmentHeadId(employee2.getId()));
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        //Create request => not autoapprove because emplyee not exists
        assertThat(tripRequestApprovalRepository.count()).isZero();
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        var tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();
    }

    @Test
    @DisplayName("Происходит автосогласование, так как actor id совпадает с head id")
    void testAutoApproveByDepartmentHead() {
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
        //Create request => not autoapprove because emplyee not exists
        assertThat(tripRequestApprovalRepository.count()).isZero();
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        var tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();

        // Create department with head
        final var employee = employeeRepository.findAll().getFirst();

        final var departmentMessage = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .organizationId(ORGANIZATION_1_ID)
                .departmentHeadId(UUID.randomUUID())
                .departmentName("Test department")
                .id(employee.getDepartmentId())
                .departmentHeadId(employee.getId())
                .build();
        produceMessage("service.organization.department", departmentMessage);
        assertThat(departmentRepository.count()).isEqualTo(1);

        tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(tripApproval.getApprovedById()).isEqualTo(requestMessage.getPassengerId());
    }

    @Test
    @DisplayName("Происходит автосогласование, так как actor id совпадает с head id. Статус заявки EDIT")
    void testAutoApproveByDepartmentHeadEditStatus() {
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
        //Create request => not autoapprove because emplyee not exists
        assertThat(tripRequestApprovalRepository.count()).isZero();
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        var tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();
        tripApproval.setStatus(Status.EDITED);
        tripRequestApprovalRepository.save(tripApproval);

        // Create department with head
        final var employee = employeeRepository.findAll().getFirst();

        final var departmentMessage = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .organizationId(ORGANIZATION_1_ID)
                .departmentHeadId(UUID.randomUUID())
                .departmentName("Test department")
                .id(employee.getDepartmentId())
                .departmentHeadId(employee.getId())
                .build();
        produceMessage("service.organization.department", departmentMessage);
        assertThat(departmentRepository.count()).isEqualTo(1);

        tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(tripApproval.getApprovedById()).isEqualTo(requestMessage.getPassengerId());
    }

    @Test
    @DisplayName("Происходит автосогласование, так как actor id совпадает с head id. Статус заявки EDIT")
    void testNoAutoApproveByDepartmentHeadCancelledStatus() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        var department = departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isSelfApproved), false)
                .set(field(Position::isActive), true)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var employee2 = employeeRepository.save(sharedData.createEmployee(UUID.randomUUID(), DEPARTMENT1_ID, position.getId()));
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        departmentRepository.save(department.setDepartmentHeadId(employee2.getId()));
        //Create request => not autoapprove because emplyee not exists
        assertThat(tripRequestApprovalRepository.count()).isZero();
        var requestMessage = baseRequestMsg();
        requestMessage.setPurposeId(tripPurpose.getId());
        requestMessage.setPassengerId(employee1.getId());
        requestMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        var tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.NEW);
        assertThat(tripApproval.getApprovedById()).isNull();
        tripApproval.setStatus(Status.CANCELLED);
        tripRequestApprovalRepository.save(tripApproval);

        // Create department with head
        final var employee = employeeRepository.findAll().getFirst();

        final var departmentMessage = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .organizationId(ORGANIZATION_1_ID)
                .departmentName("Test department")
                .id(employee.getDepartmentId())
                .departmentHeadId(employee.getId())
                .build();
        produceMessage("service.organization.department", departmentMessage);
        assertThat(departmentRepository.count()).isEqualTo(1);

        // Not approved because status was CANCELLED
        tripApproval = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripApproval.getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(tripApproval.getApprovedById()).isNull();
    }
}
