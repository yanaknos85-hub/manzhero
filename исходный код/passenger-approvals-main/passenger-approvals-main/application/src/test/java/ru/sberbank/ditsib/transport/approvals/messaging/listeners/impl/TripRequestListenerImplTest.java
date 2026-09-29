package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.approvals.messaging.ApproveFinalTripMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;
import ru.sberbank.ditsib.transport.approvals.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;
import static ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl.ApprovalTestUtils.*;
import static ru.sberbank.ditsib.transport.constants.PublicCompensationType.SUBURB_TRIP_COMPENSATION;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.*;

@EmbeddedPostgres
@DisplayName("Проверка получения заявок на поездку")
@MockitoBean(types = {JwtDecoder.class})
class TripRequestListenerImplTest extends KafkaTest {

    @Autowired
    @Qualifier("requestInput")
    private Consumer<Message<RequestMessage>> requestInput;

    @MockitoBean("finalTripApproveOutput")
    private OutputBridge finalTripApproveOutput;

    @Autowired
    private TripRequestApprovalRepository tripRequestApprovalRepository;

    @Autowired
    private FinalTripApprovalRepository finalTripApprovalRepository;

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
    private DelegateRepository delegateRepository;

    @Autowired
    private DepLimitRepository depLimitRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private TaxiTariffRepository taxiTariffRepository;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Autowired
    private TripPurposeRepository purposeRepository;

    @Autowired
    private TaxiApprovalsSettingsRepository taxiApprovalsSettingsRepository;

    @Autowired
    private PublicTrApprovalsSettingsRepository publicApprovalsSettingsRepository;

    @Autowired
    private OtherTrTypesApprovalsSettingsRepository otherApprovalsSettingsRepository;

    @Autowired
    private TariffMapper tariffMapper;

    @Autowired
    private FraudDataRepository fraudRepository;
    @Autowired
    private ApprovalJournalRepository approvalJournalRepository;

    private final TestSharedData sharedData = new TestSharedData();

    private Organization org1;
    private Employee employee1;
    private TripPurpose tripPurpose1;
    private TripPurpose tripPurpose2;
    private GeoZone region2;
    private GeoZone region3;
    private TaxiTariff taxiTariff1;
    private TaxiApprovalsSettings taxiApprovalsSettings1;

    void initForTaxiApprovalsSettings(boolean needApproval, int minCostKop, int minCostKop1, int minCostKop2) {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        tripPurpose1 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        tripPurpose2 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE2_ID));
        purposeRepository.save(sharedData.createTripPurpose(PURPOSE3_ID));
        var region1 = geoZoneRepository.save(sharedData.createGeoZone(REGION1_ID));
        region2 = geoZoneRepository.save(sharedData.createGeoZone(REGION2_ID));
        region3 = geoZoneRepository.save(sharedData.createGeoZone(REGION3_ID));
        taxiTariff1 = taxiTariffRepository.save(tariffMapper.messageToEntity(sharedData.createTaxiTariffMessage(
                TARIFF1_ID, REGION1_ID, null, CONTRACT1_ID, ORGANIZATION_1_ID, TaxiClass.ECONOMY, false)));
        var item1 = sharedData.createSettingsItem(region1, minCostKop1, tripPurpose1);
        var item2 = sharedData.createSettingsItem(region2, minCostKop2, tripPurpose2);
        taxiApprovalsSettings1 = taxiApprovalsSettingsRepository.save(
                sharedData.createTaxiSettings(needApproval, minCostKop, org1, "TAXI", List.of(item1, item2)));
    }

    void initForPublicApprovalSettings() {
        initForTaxiApprovalsSettings(true, 0, 0, 0);

        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        publicApprovalsSettingsRepository.save(sharedData.createPublicSettings(
                org1, "PUBLIC", true, true, true,
                false));
    }

    void initForOthersApprovalSettings() {
        initForTaxiApprovalsSettings(true, 0, 0, 0);

        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        otherApprovalsSettingsRepository.save(sharedData.createOtherTrSettings(
                true, 0, org1, "PERSONAL", null, false));
    }

    @AfterEach
    void afterEach() {
        taxiApprovalsSettingsRepository.deleteAll();
        publicApprovalsSettingsRepository.deleteAll();
        otherApprovalsSettingsRepository.deleteAll();
        tariffRepository.deleteAll();
        taxiTariffRepository.deleteAll();
        geoZoneRepository.deleteAll();
        tripRequestApprovalRepository.deleteAll();
        approvalJournalRepository.deleteAll();
        finalTripApprovalRepository.deleteAll();
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department
                .setDepartmentHeadId(null)
                .setParentId(null)));
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
        fraudRepository.deleteAll();
        purposeRepository.deleteAll();
    }

    @Test
    @DisplayName("Новая")
    @Disabled("Требуется актуализация")
    void test_new() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var message = baseRequestWithSharedRideMsg();
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        message.setFinishedTime(LocalDateTime.now().plusDays(3));
        createApprovers(message.getPassengerId());

        assertThat(tripRequestApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = tripRequestApprovalRepository.findAll().getFirst();

        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
        assertThat(actual.getApprovedById()).isNull();
        assertThat(actual.getTransportType()).isEqualTo(message.getTransportType());
        assertThat(actual.getTaxiClass().name()).isEqualTo(message.getTripClass());
        assertThat(actual.getCost()).isEqualTo(message.getExpected().getCost());
        assertThat(actual.getDesiredDate()).isCloseTo(message.getDesiredDate(), within(1, ChronoUnit.SECONDS));
        assertThat(actual.getPurposeId()).isEqualTo(message.getPurposeId());
        assertThat(actual.getSharedRideId()).isEqualTo(message.getRideId());
        assertThat(actual.getSharedRideOwner()).isEqualTo(message.isSharedRideOwner());
    }


    @Test
    @DisplayName("Новая self approved")
    void test_new_self_approved() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), true)
                .create()
        );
        var employee = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        tripPurpose1 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        var message = baseRequestMsg();
        message.setPurposeId(tripPurpose1.getId());
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee.getDepartmentId())
                .userId(employee.getUserId())
                .build());
        assertThat(tripRequestApprovalRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(actual.getApprovedById()).isEqualTo(message.getPassengerId());
    }

    @Test
    @DisplayName("Сохранение фродовых данных")
    void test_fraud() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), true)
                .create()
        );
        var employee = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        tripPurpose1 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        var message = baseRequestMsg();
        var fraud = new RequestMessage.Fraud("SPLIT", "fraud", message.getId());
        var fraud2 = new RequestMessage.Fraud("SPLIT", "fraud2", message.getId());
        message.setPurposeId(tripPurpose1.getId());
        message.setFraudData(List.of(fraud));
        message.setPassengerId(employee.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee.getDepartmentId())
                .userId(employee.getUserId())
                .build());
        assertThat(tripRequestApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        assertThat(fraudRepository.count()).isEqualTo(1);

        var message2 = baseRequestMsg();
        message2.setPurposeId(tripPurpose1.getId());
        message2.setId(message.getId());
        message2.setFraudData(List.of(fraud, fraud2));
        message2.setPassengerId(employee.getId());
        message2.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee.getDepartmentId())
                .userId(employee.getUserId())
                .build());

        requestInput.accept(MessageBuilder.withPayload(message2).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        assertThat(fraudRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Сохранение фродовых данных для FINAL_APPROVE_STATUSES")
    void test_FINAL_APPROVE_STATUSES_fraud() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
            .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
            .set(field(Position::isActive), true)
            .set(field(Position::isSelfApproved), true)
            .create()
        );
        var employee = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        tripPurpose1 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        var message = baseRequestMsg();
        message.setPurposeId(tripPurpose1.getId());
        var fraud = new RequestMessage.Fraud("SPLIT", "fraud", message.getId());
        var fraud2 = new RequestMessage.Fraud("SPLIT", "fraud2", message.getId());
        message.setStatus(PERSONAL_AWAITING_TRIP_APPROVAL.name());
        message.setFraudData(List.of(fraud));
        message.setPassengerId(employee.getId());
        message.setPassenger(RequestMessage.Employee
            .builder()
            .departmentId(employee.getDepartmentId())
            .userId(employee.getUserId())
            .build());
        assertThat(finalTripApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(finalTripApprovalRepository.count()).isEqualTo(1);
        assertThat(fraudRepository.count()).isEqualTo(1);

        var message2 = baseRequestMsg();
        message2.setPurposeId(tripPurpose1.getId());
        message2.setStatus(PERSONAL_AWAITING_TRIP_APPROVAL.name());
        message2.setId(message.getId());
        message2.setFraudData(List.of(fraud, fraud2));
        message2.setPassengerId(employee.getId());
        message2.setPassenger(RequestMessage.Employee
            .builder()
            .departmentId(employee.getDepartmentId())
            .userId(employee.getUserId())
            .build());

        requestInput.accept(MessageBuilder.withPayload(message2).build());
        assertThat(finalTripApprovalRepository.count()).isEqualTo(1);
        assertThat(fraudRepository.count()).isEqualTo(2);
    }

    @Test
    @Disabled("Можно включить после завершения миграции и удаления кода поддержки миграции")
    @DisplayName("Новая не создается, потому что пришел request не в статусе AWAITING_APPROVAL")
    void test_new_skip_not_awaiting_approval() {
        var message = baseRequestMsg();
        message.setFraudData(Collections.emptyList());
        message.setStatus(TAXI_TRIP_IN_PROGRESS.name());
        assertThat(tripRequestApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isZero();
    }

    @Test
    @DisplayName("Миграция: новая создается в статусе APPROVED")
    @Disabled
    void test_new_migration_approved() {
        var message = baseRequestMsg();
        message.setFraudData(Collections.emptyList());
        message.setStatus(TAXI_TRIP_IN_PROGRESS.name());

        assertThat(tripRequestApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());

        var actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    @DisplayName("Миграция: новая создается в статусе CANCELLED")
    @Disabled
    void test_new_migration_cancelled() {
        var message = baseRequestMsg();
        message.setStatus(TAXI_CANCELLED.name());

        assertThat(tripRequestApprovalRepository.count()).isZero();

        requestInput.accept(MessageBuilder.withPayload(message).build());

        var actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    @DisplayName("Новая. Пассажир является руководителем подразделения")
    void test_new_department_head() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        final var employee = createEmployee();
        // set department head
        final var department = departmentRepository.findById(employee.getDepartmentId()).orElseThrow();
        departmentRepository.save(department.setDepartmentHeadId(employee.getId()));
        tripPurpose1 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));

        var message = baseRequestMsg();
        message.setPurposeId(tripPurpose1.getId());
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee.getDepartmentId())
                .userId(employee.getUserId())
                .build());
        assertThat(tripRequestApprovalRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(actual.getApprovedById()).isEqualTo(message.getPassengerId());
    }

    private Employee createEmployee() {
        var organization = organizationRepository.save(Instancio.of(Organization.class)
                .set(field(Organization::isActive), true)
                .set(field(Organization::getDigitId), 123L)
                .create());
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organization.getId())
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        organizationRepository.save(organization);
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organization.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        return employeeRepository.save(employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create()));
    }

    @Test
    @DisplayName("Новая. Поездка завершена")
    void test_new_finished() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        tripPurpose1 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        var message = baseRequestMsg();
        message.setFraudData(Collections.emptyList());

        assertThat(tripRequestApprovalRepository.count()).isZero();
        message.setPurposeId(tripPurpose1.getId());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee.builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = tripRequestApprovalRepository.findAll().getFirst();

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
    @DisplayName("Изменение")
    void test_edit() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        tripPurpose1 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        var actionId = UUID.randomUUID();

        var approval = new TripRequestApproval();
        approval.setActionId(actionId);
        approval.setCreationTime(LocalDateTime.now().minusDays(10));
        approval.setAuthorId(UUID.randomUUID());
        approval.setStatus(Status.APPROVED);
        approval.setActorId(employee1.getId());
        approval.setPurposeId(tripPurpose1.getId());
        tripRequestApprovalRepository.save(approval);
        var message = baseRequestMsg();
        message.setPurposeId(tripPurpose1.getId());
        message.setFraudData(Collections.emptyList());
        message.setId(actionId);
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = tripRequestApprovalRepository.findAll().getFirst();

        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(message.getCreationTime(), within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(actual.getApprovedById()).isNull();
    }

    @Test
    @DisplayName("Изменение. Поездка завершена")
    void test_edit_tripFinished() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        tripPurpose1 = purposeRepository.save(sharedData.createTripPurpose(PURPOSE1_ID));
        var actionId = UUID.randomUUID();

        var approval = new TripRequestApproval();
        approval.setActionId(actionId);
        approval.setCreationTime(LocalDateTime.now().minusDays(10));
        approval.setAuthorId(UUID.randomUUID());
        approval.setStatus(Status.APPROVED);
        approval.setActorId(employee1.getId());
        approval.setPurposeId(tripPurpose1.getId());
        tripRequestApprovalRepository.save(approval);
        var message = baseRequestMsg();
        message.setPurposeId(tripPurpose1.getId());
        message.setFraudData(Collections.emptyList());
        message.setId(actionId);
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        message.setFinishedTime(LocalDateTime.now().plusDays(3));

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = tripRequestApprovalRepository.findAll().getFirst();

        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getActorId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(message.getCreationTime(), within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(actual.getApprovedById()).isNull();
    }

    @Test
    @DisplayName("Удаление")
    @Disabled("Требуется актуализация")
    void test_delete() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        var actionId = UUID.randomUUID();

        var approval = new TripRequestApproval();
        approval.setActionId(actionId);
        approval.setAuthorId(UUID.randomUUID());
        approval.setCreationTime(LocalDateTime.now().minusDays(100));
        approval.setStatus(Status.NEW);
        approval.setActorId(employee1.getId());
        tripRequestApprovalRepository.save(approval);

        UpdateTripRequestApproval updateApproval = new UpdateTripRequestApproval();
        updateApproval.setActionId(actionId);
        updateApproval.setAuthorId(UUID.randomUUID());
        updateApproval.setCreationTime(LocalDateTime.now().minusDays(100));
        updateApproval.setStatus(Status.NEW);
        updateApproval.setActorId(UUID.randomUUID());
        updateApproval.setUpdateId(UUID.randomUUID());
        updateApproval.setTransportType("TAXI");
        updateApproval.setDesiredDate(LocalDateTime.now());
        updateApproval.setPurposeId(UUID.randomUUID());
        updateTripRequestApprovalRepository.save(updateApproval);

        updateApproval = new UpdateTripRequestApproval();
        updateApproval.setActionId(actionId);
        updateApproval.setAuthorId(UUID.randomUUID());
        updateApproval.setCreationTime(LocalDateTime.now().minusDays(100));
        updateApproval.setStatus(Status.NEW);
        updateApproval.setActorId(UUID.randomUUID());
        updateApproval.setUpdateId(UUID.randomUUID());
        updateApproval.setTransportType("TAXI");
        updateApproval.setDesiredDate(LocalDateTime.now());
        updateApproval.setPurposeId(UUID.randomUUID());
        updateTripRequestApprovalRepository.save(updateApproval);
        assertThat(updateTripRequestApprovalRepository.count()).isEqualTo(2);

        var message = baseRequestMsg();
        message.setFraudData(Collections.emptyList());
        message.setId(actionId);
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        message.setDeleted(true);

        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);

        var actual = tripRequestApprovalRepository.findAll().getFirst();

        assertThat(actual.getActionId()).isEqualTo(message.getId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(approval.getCreationTime(), within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(approval.getAuthorId());
        assertThat(actual.getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(actual.getApprovedById()).isNull();

        assertThat(updateTripRequestApprovalRepository.count()).isEqualTo(2);
        final List<UpdateTripRequestApproval> all = updateTripRequestApprovalRepository.findAll();
        assertThat(all.get(0).getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(all.get(1).getStatus()).isEqualTo(Status.CANCELLED);

    }

    @Test
    @DisplayName("Проверка настроек согласований такси - автосогласование по флагу")
    void taxiApprovalsSettingsTest_notNeedApprove() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        initForTaxiApprovalsSettings(false, 0, 0, 0);
        // cost в копейках, несмотря на тип double
        RequestMessage message = createCustomRequestMessage(5000., TARIFF1_ID, "TAXI", PURPOSE1_ID);
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee.builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        TripRequestApproval actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getApprovedById()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    @DisplayName("Проверка настроек согласований такси - автосогласование по основной сумме")
    void taxiApprovalsSettingsTest_byCommonCost() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // преобразуем настройки так, чтобы они срабатывали по основной сумме
        initForTaxiApprovalsSettings(true, 5000, 0, 0);
        taxiApprovalsSettings1.setPurposeAndRegionItems(null);
        taxiApprovalsSettingsRepository.save(taxiApprovalsSettings1);
        // все, что меньше 5000 коп - автосогласование. cost в копейках, несмотря на тип double
        RequestMessage message = createCustomRequestMessage(4990., TARIFF1_ID, "TAXI", PURPOSE1_ID);
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee.builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        TripRequestApproval actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getApprovedById()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    @DisplayName("Проверка настроек согласований такси - автосогласование по сумме из доп.настройки (геозона + цель)")
    void taxiApprovalsSettingsTest_byGeoZoneAndPurposeCost() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // зададим настройки так, чтобы они срабатывали по сумме из доп.настроек
        // для этого должен сработать фильтр O1: R1+P1 (Organization1: Region1 + Purpose1)
        initForTaxiApprovalsSettings(true, 4000, 6000, 7000);
        // все, что меньше 6000 коп (60 руб) - автосогласование по фильтру R1+P1. cost в копейках, несмотря на тип double
        RequestMessage message = createCustomRequestMessage(4990., TARIFF1_ID, "TAXI", PURPOSE1_ID);
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee.builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        TripRequestApproval actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getApprovedById()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    @DisplayName("Проверка настроек согласований такси - автосогласование по осн.сумме после доп.настроек")
    void taxiApprovalsSettingsTest_byCommonCostAfterGeoZoneAndPurposeFilter() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // зададим настройки так, чтобы они не срабатывали по сумме из доп.настроек
        // для этого не должен сработать ни один фильтр O1: Ri+Pi
        initForTaxiApprovalsSettings(true, 5000, 6000, 7000);
        // зададим цель, которой нет в фильтрах. cost в копейках, несмотря на тип double
        RequestMessage message = createCustomRequestMessage(4990., TARIFF1_ID, "TAXI", PURPOSE3_ID);
        message.setFraudData(Collections.emptyList());
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee.builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(message).build());
        // ни один фильтр не сработает, но автосогласование - по основной сумме
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        TripRequestApproval actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getApprovedById()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    @DisplayName("Проверка настроек согласований такси - автосогласование по доп.настройке (геозона = null + цель)")
    void taxiApprovalsSettingsTest_byNullGeoZoneAndPurposeCost() {
        org1 = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, org1.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // зададим настройки так, чтобы они срабатывали по сумме из доп.настроек при геозоне = null
        // для этого должен сработать фильтр O1: Region=null + P1
        initForTaxiApprovalsSettings(true, 4000, 6000, 7000);
        // для цели 1 установить любую геозону, для сообщения - регион 3
        PurposeAndRegionApprovalSettingsItem item1 = sharedData.createSettingsItem(null, 6000, tripPurpose1);
        PurposeAndRegionApprovalSettingsItem item2 = sharedData.createSettingsItem(region2, 7000, tripPurpose2);
        taxiApprovalsSettings1.setPurposeAndRegionItems(List.of(item1, item2));
        taxiApprovalsSettings1 = taxiApprovalsSettingsRepository.save(taxiApprovalsSettings1);
        assertThat(taxiApprovalsSettings1.getPurposeAndRegionItems()).hasSize(2);
        // cost в копейках, несмотря на тип double
        RequestMessage message = createCustomRequestMessage(4990., TARIFF1_ID, "TAXI", PURPOSE1_ID);
        message.setFraudData(Collections.emptyList());
        taxiTariff1.setRegionId(region3.getId());
        taxiTariffRepository.save(taxiTariff1);
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee.builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(message).build());
        // должен отработать фильтр по P1
        assertThat(tripRequestApprovalRepository.count()).isEqualTo(1);
        TripRequestApproval actual = tripRequestApprovalRepository.findAll().getFirst();
        assertThat(actual.getApprovedById()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    @DisplayName("Проверка настроек согласований ОТ - отключено утверждение")
    void publicApprovalsSettingsTest_AffirmativeDisabled() {
        initForPublicApprovalSettings();
        var message = createCustomRequestMessage(0., TARIFF1_ID, TransportTypeEnum.PUBLIC.name(), UUID.randomUUID());
        message.setPurposeId(tripPurpose1.getId());
        message.setFraudData(List.of(new RequestMessage.Fraud("SPLIT", "fraud", message.getId())));
        message.setStatus(PUBLIC_AWAITING_AFFIRMATIVE.name());
        message.setTransportCompensation(List.of(RequestMessage.TransportCompensation.builder()
                .compensationType(SUBURB_TRIP_COMPENSATION.name())
                .transportType(TransportTypeEnum.PUBLIC.name())
                .ticketsCost(40)
                .build()));
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee.builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());

        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(finalTripApprovalRepository.count()).isEqualTo(1);
        var actual = finalTripApprovalRepository.findAll().getFirst();
        assertThat(actual.getCreationTime()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(actual.getTransportType()).isEqualTo("PUBLIC");

        var actualMessageCaptor = ArgumentCaptor.forClass(ApproveFinalTripMessage.class);
        verify(finalTripApproveOutput).send(actualMessageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, message.getId())));
        ApproveFinalTripMessage actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.requestId()).isEqualTo(actual.getActionId());
        assertThat(actualMessage.actorEmployeeId()).isEqualTo(actual.getActorId());
        assertThat(actualMessage.approved()).isTrue();
        assertThat(fraudRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Проверка настроек согласований на личном тр-те - отключено утверждение маршрута")
    void othersApprovalsSettingsTest_TripConfirmDisabled() {
        initForOthersApprovalSettings();
        var message = createCustomRequestMessage(0., TARIFF1_ID, "PERSONAL", UUID.randomUUID());
        message.setPurposeId(tripPurpose1.getId());
        message.setFraudData(Collections.emptyList());
        message.setStatus(PERSONAL_AWAITING_TRIP_APPROVAL.name());
        message.setTransportCompensation(List.of(RequestMessage.TransportCompensation.builder()
                .compensationType(null)
                .transportType(TransportTypeEnum.PUBLIC.name())
                .ticketsCost(40)
                .build()));
        message.setPassengerId(employee1.getId());
        message.setPassenger(RequestMessage.Employee.builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        requestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(finalTripApprovalRepository.count()).isEqualTo(1);
        var actual = finalTripApprovalRepository.findAll().getFirst();
        assertThat(actual.getCreationTime()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(actual.getTransportType()).isEqualTo("PERSONAL");

        var actualMessageCaptor = ArgumentCaptor.forClass(ApproveFinalTripMessage.class);
        verify(finalTripApproveOutput).send(actualMessageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, message.getId())));
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.requestId()).isEqualTo(actual.getActionId());
        assertThat(actualMessage.actorEmployeeId()).isEqualTo(actual.getActorId());
        assertThat(actualMessage.approved()).isTrue();
    }

    private void createApprovers(UUID passengerId) {
        var organization = organizationRepository.save(Instancio.of(Organization.class)
                .set(field(Organization::isActive), true)
                .set(field(Organization::getDigitId), 123L)
                .create());
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organization.getId())
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        organizationRepository.save(organization);
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organization.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        employeeRepository.save(employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getId), passengerId)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create()));
        var head = employeeRepository.save(employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create()));
        departmentRepository.save(department.setDepartmentHeadId(head.getId()));

        var depLimit = DepLimit.builder()
                .id(UUID.randomUUID())
                .ownerId(head.getId())
                .year(LocalDate.now().getYear())
                .sum(100L)
                .reserve(10L)
                .build();
        depLimitRepository.save(depLimit);
        var delegate = Delegate.builder()
                .id(UUID.randomUUID())
                .delegateId(UUID.randomUUID())
                .supervisorId(head.getId())
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build();
        delegateRepository.save(delegate);
    }
}