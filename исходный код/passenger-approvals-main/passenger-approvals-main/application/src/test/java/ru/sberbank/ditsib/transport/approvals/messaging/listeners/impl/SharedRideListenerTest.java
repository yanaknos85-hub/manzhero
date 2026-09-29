package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.database.model.Position;
import ru.sberbank.ditsib.transport.approvals.database.model.Status;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.Tariff;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.instancio.Select.field;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;
import static ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl.ApprovalTestUtils.baseRequestWithSharedRideMsg;

@EmbeddedPostgres
@DisplayName("Проверка получения заявок на согласование присоединения к совместной поездке")
@MockitoBean(types = {JwtDecoder.class})
@Disabled("Требуется переработка")
class SharedRideListenerTest extends KafkaTest {

    @Autowired
    @Qualifier("requestInput")
    private Consumer<Message<RequestMessage>> requestInput;

    @Autowired
    private SharedRideApprovalRepository repository;

    @Autowired
    private TripRequestApprovalRepository tripRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TariffRepository tariffRepository;

    private final TestSharedData sharedData = new TestSharedData();

    private Tariff tariff;

    @AfterEach
    void afterEach() {
        repository.deleteAll();
        tripRequestRepository.deleteAll();
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    @BeforeEach
    void beforeEach() {
        final var organization = organizationRepository.save(Instancio.create(Organization.class));
        tariff = tariffRepository.save(Instancio.of(Tariff.class)
                .set(Select.field(Tariff::getOrganizationId), organization.getId())
                .create());
    }

    @Test
    @DisplayName("Новое согласование для статуса AWAITING_SHARED_RIDE_APPROVAL")
    void testNewSharedRideApproval() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // Create trip request approval - owner of shared ride
        var ownerMessage = baseRequestWithSharedRideMsg();
        ownerMessage.setPassengerId(employee1.getId());
        ownerMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        ownerMessage.setTariffId(tariff.getId());
        ownerMessage.setTransportType("PERSONAL");
        assertThat(tripRequestRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(ownerMessage).build());
        assertThat(tripRequestRepository.count()).isEqualTo(1);
        var tripRequestApproval = tripRequestRepository.findAll().get(0);
        assertThat(tripRequestApproval.getSharedRideOwner()).isTrue();

        // Create shared ride approval (отсылка сообщения-заявки, которая присоединяется к owner заявке)
        var addedMessage = baseRequestWithSharedRideMsg();
        addedMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        addedMessage.setTariffId(tariff.getId());
        addedMessage.setStatus(TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL.name());
        addedMessage.setTransportType("PERSONAL");
        addedMessage.setSharedRideOwner(false);

        // Set the same shared request id
        addedMessage.setRideId(ownerMessage.getRideId());
        assertThat(repository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(addedMessage).build());
        assertThat(repository.count()).isEqualTo(1);

        var actual = repository.findAll().get(0);

        assertThat(actual.getActionId()).isEqualTo(ownerMessage.getId());
        assertThat(actual.getActorId()).isEqualTo(ownerMessage.getPassengerId());
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isCloseTo(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                within(1, ChronoUnit.MINUTES));
        assertThat(actual.getAuthorId()).isEqualTo(ownerMessage.getAuthorId());
        assertThat(actual.getTransportType()).isEqualTo(ownerMessage.getTransportType());
        assertThat(actual.getTaxiClass().name()).isEqualTo(ownerMessage.getTripClass());
        assertThat(actual.getCost()).isEqualTo(ownerMessage.getExpected().getCost());
        assertThat(actual.getDesiredDate()).isCloseTo(ownerMessage.getDesiredDate(), within(1, ChronoUnit.SECONDS));
        assertThat(actual.getPurposeId()).isEqualTo(ownerMessage.getPurposeId());

        assertThat(actual.getStatus()).isEqualTo(Status.NEW);
        assertThat(actual.getAddRequestId()).isEqualTo(addedMessage.getId());
        assertThat(actual.getActionId()).isNotEqualTo(actual.getAddRequestId());
    }

    @Test
    @DisplayName("Новое согласование не создается, так как не найден sharedOwner trip request approval")
    void testNotCreateSharedRideApprovalNotFoundOwner() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // Create trip request approval - owner of shared ride
        var ownerMessage = baseRequestWithSharedRideMsg();
        ownerMessage.setPassengerId(employee1.getId());
        ownerMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        ownerMessage.setSharedRideOwner(false);
        ownerMessage.setTransportType("PERSONAL");
        assertThat(tripRequestRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(ownerMessage).build());
        assertThat(tripRequestRepository.count()).isEqualTo(1);
        var tripRequestApproval = tripRequestRepository.findAll().get(0);
        assertThat(tripRequestApproval.getSharedRideOwner()).isFalse();

        // Create shared ride approval (отсылка сообщения-заявки, которая присоединяется к owner заявке)
        var addedMessage = baseRequestWithSharedRideMsg();
        addedMessage.setStatus(TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL.name());
        addedMessage.setSharedRideOwner(false);
        addedMessage.setTransportType("PERSONAL");
        // Set the same shared request id
        addedMessage.setRideId(ownerMessage.getRideId());
        assertThat(repository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(addedMessage).build());
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("Новое согласование не создается, так как не request не в статусе AWAITING_SHARED_RIDE_APPROVAL")
    void testNotCreateSharedRideApprovalAnotherStatus() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // Create trip request approval - owner of shared ride
        var ownerMessage = baseRequestWithSharedRideMsg();
        ownerMessage.setPassengerId(employee1.getId());
        ownerMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        ownerMessage.setTariffId(tariff.getId());
        ownerMessage.setTransportType("PERSONAL");
        assertThat(tripRequestRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(ownerMessage).build());
        assertThat(tripRequestRepository.count()).isEqualTo(1);
        var tripRequestApproval = tripRequestRepository.findAll().get(0);
        assertThat(tripRequestApproval.getSharedRideOwner()).isTrue();

        // Create shared ride approval (отсылка сообщения-заявки, которая присоединяется к owner заявке)
        var addedMessage = baseRequestWithSharedRideMsg();
        addedMessage.setStatus(TripRequestStatus.PERSONAL_AWAITING_APPROVAL.name());
        addedMessage.setSharedRideOwner(false);
        addedMessage.setTransportType("PERSONAL");
        // Set the same shared request id
        addedMessage.setRideId(ownerMessage.getRideId());
        assertThat(repository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(addedMessage).build());
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("Создается новое согласование для статуса AWAITING_SHARED_RIDE_APPROVAL. Потом заявка владельца " +
            "личного транспорта отменяется")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testCancelSharedRideOwnerApproval() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // Create trip request approval - owner of shared ride
        var ownerMessage = baseRequestWithSharedRideMsg();
        ownerMessage.setPassengerId(employee1.getId());
        ownerMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        ownerMessage.setTariffId(tariff.getId());
        ownerMessage.setTransportType("PERSONAL");
        assertThat(tripRequestRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(ownerMessage).build());
        assertThat(tripRequestRepository.count()).isEqualTo(1);
        var tripRequestApproval = tripRequestRepository.findAll().get(0);
        assertThat(tripRequestApproval.getSharedRideOwner()).isTrue();

        // Create shared ride approval (отсылка сообщения-заявки, которая присоединяется к owner заявке)
        var addedMessage = baseRequestWithSharedRideMsg();
        addedMessage.setStatus(TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL.name());
        addedMessage.setSharedRideOwner(false);
        addedMessage.setTransportType("PERSONAL");
        // Set the same shared request id
        addedMessage.setRideId(ownerMessage.getRideId());
        assertThat(repository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(addedMessage).build());
        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.NEW);

        // Cancel owner request
        ownerMessage.setStatus(TripRequestStatus.PERSONAL_CANCELLED.name());
        requestInput.accept(MessageBuilder.withPayload(ownerMessage).build());
        assertThat(tripRequestRepository.count()).isEqualTo(1);
        tripRequestApproval = tripRequestRepository.findAll().get(0);
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(repository.count()).isEqualTo(1);
        actual = repository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    @DisplayName("Создается новое согласование для статуса AWAITING_SHARED_RIDE_APPROVAL. Потом присоединенная заявка" +
            " отменяется")
    void testCancelSharedRideAddedRequestApproval() {
        var organization = organizationRepository.save(sharedData.createOrganization(ORGANIZATION_1_ID));
        departmentRepository.save(sharedData.createDepartment(DEPARTMENT1_ID, organization.getId()));
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), ORGANIZATION_1_ID)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var employee1 = employeeRepository.save(sharedData.createEmployee(EMPLOYEE1_ID, DEPARTMENT1_ID, position.getId()));
        // Create trip request approval - owner of shared ride
        var ownerMessage = baseRequestWithSharedRideMsg();
        ownerMessage.setPassengerId(employee1.getId());
        ownerMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        ownerMessage.setTariffId(tariff.getId());
        ownerMessage.setTransportType("PERSONAL");
        assertThat(tripRequestRepository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(ownerMessage).build());
        assertThat(tripRequestRepository.count()).isEqualTo(1);
        var tripRequestApproval = tripRequestRepository.findAll().get(0);
        assertThat(tripRequestApproval.getSharedRideOwner()).isTrue();

        // Create shared ride approval (отсылка сообщения-заявки, которая присоединяется к owner заявке)
        var addedMessage = baseRequestWithSharedRideMsg();
        addedMessage.setStatus(TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL.name());
        addedMessage.setSharedRideOwner(false);
        addedMessage.setTransportType("PERSONAL");
        addedMessage.setPassenger(RequestMessage.Employee
                .builder()
                .departmentId(employee1.getDepartmentId())
                .userId(employee1.getUserId())
                .build());
        // Set the same shared request id
        addedMessage.setRideId(ownerMessage.getRideId());
        assertThat(repository.count()).isZero();
        requestInput.accept(MessageBuilder.withPayload(addedMessage).build());
        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.EDITED);

        // Cancel added request
        addedMessage.setStatus(TripRequestStatus.PERSONAL_CANCELLED.name());
        requestInput.accept(MessageBuilder.withPayload(addedMessage).build());
        assertThat(repository.count()).isEqualTo(1);
        actual = repository.findAll().get(0);
        assertThat(actual.getStatus()).isEqualTo(Status.CANCELLED);
    }


}
