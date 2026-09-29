package ru.sber.transport.fraud.monitoring.providers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.records.*;
import ru.sber.transport.fraud.monitoring.model.*;
import ru.sber.transport.fraud.monitoring.providers.model.TestTripRequest;
import ru.sber.transport.fraud.monitoring.providers.trip_request.TripRequestsDatabaseDatabaseProviderImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка провайдера заявок на поездки")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class, TripRequestsDatabaseDatabaseProviderImpl.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class TripRequestsDatabaseProviderImplTest {

    @MockitoBean
    private DepartmentsDatabaseProvider departmentsDatabaseProvider;

    @MockitoBean
    private EmployeesDatabaseProvider employeesProvider;

    @MockitoBean
    private TripPurposesDatabaseProvider tripPurposesProvider;

    @MockitoBean
    private WaypointsDatabaseProvider waypointsDatabaseProvider;

    @Autowired
    private DSLContext context;

    @Autowired
    private TripRequestsDatabaseProvider tripRequestsDatabaseProvider;


    @Test
    @DisplayName("Проверка сохранения заявки")
    void test_createOrUpdate() {
        final var source = Instancio.of(TestTripRequest.class)
                .set(field(TestTripRequest::id), UUID.randomUUID())
                .set(field(TestTripRequest::humanReadableId), "OT-0001-00014844")
                .set(field(TestTripRequest::transportType), TransportType.PUBLIC)
                .set(field(TestTripRequest::tariff), "BUSINESS")
                .set(field(TestTripRequest::passengerId), UUID.randomUUID())
                .set(field(TestTripRequest::approverId), UUID.randomUUID())
                .set(field(TestTripRequest::desiredDate), OffsetDateTime.now())
                .set(field(TestTripRequest::approvalDate), OffsetDateTime.now())
                .set(field(TestTripRequest::plannedCost), new BigDecimal("150.75"))
                .set(field(TestTripRequest::actualCost), new BigDecimal("100.50"))
                .set(field(TestTripRequest::organizationId), UUID.randomUUID())
                .set(field(TestTripRequest::departmentId), UUID.randomUUID())
                .set(field(TestTripRequest::requestStatus), "NEW")
                .set(field(TestTripRequest::purposeId), UUID.randomUUID())
                .set(field(TestTripRequest::timeZone), "+03:00")
                .set(field(TestTripRequest::waypoints), Instancio.ofList(Waypoint.class).size(3).create())
                .set(field(TestTripRequest::distance), 1500.0)
                .set(field(TestTripRequest::compensationType), "CITY_TRIP_COMPENSATION")
                .set(field(TestTripRequest::duration), 120L)
                .create();

        final var actual = tripRequestsDatabaseProvider.createOrUpdate(source);

        assertSoftly(it -> {
            it.assertThat(actual).isNotNull();
            it.assertThat(actual.getId()).isEqualTo(source.id());
            it.assertThat(actual.getHumanReadableId()).isEqualTo(source.humanReadableId());
            it.assertThat(actual.getTransportType()).isEqualTo(source.transportType());
            it.assertThat(actual.getTariff()).isEqualTo(source.tariff());
            it.assertThat(actual.getPassengerId()).isEqualTo(source.passengerId());
            it.assertThat(actual.getApproverId()).isEqualTo(source.approverId());
            it.assertThat(actual.getDesiredDate()).isNotNull();
            it.assertThat(actual.getApprovalDate()).isNotNull();
            it.assertThat(actual.getPlannedCost()).isEqualTo(source.plannedCost());
            it.assertThat(actual.getActualCost()).isEqualTo(source.actualCost());
            it.assertThat(actual.getOrganizationId()).isEqualTo(source.organizationId());
            it.assertThat(actual.getDepartmentId()).isEqualTo(source.departmentId());
            it.assertThat(actual.getRequestStatus()).isEqualTo(source.requestStatus());
            it.assertThat(actual.getPurposeId()).isEqualTo(source.purposeId());
            it.assertThat(actual.getTimeZone()).isEqualTo(source.timeZone());
            it.assertThat(actual.getWaypoints()).hasSameSizeAs(source.waypoints());
            it.assertThat(actual.getDistance()).isEqualTo(source.distance());
            it.assertThat(actual.getCompensationType()).isEqualTo(source.compensationType());
            it.assertThat(actual.getDuration()).isEqualTo(source.duration());
        });

        assertWaypoints(actual, source);

        assertThat(context.fetchCount(Tables.TRIP_REQUEST)).isEqualTo(1);
    }

    private static void assertWaypoints(TripRequest actual, TestTripRequest source) {
        for (var i = 0; i < actual.getWaypoints().size(); i++) {
            final var waypoint = actual.getWaypoints().get(i);
            final var sourceWaypoint = source.waypoints().get(i);
            assertSoftly(it -> {
                it.assertThat(waypoint.getId()).isEqualTo(sourceWaypoint.getId());
                it.assertThat(waypoint.getTripRequestId()).isEqualTo(source.id());
                it.assertThat(waypoint.getCountry()).isEqualTo(sourceWaypoint.getCountry());
                it.assertThat(waypoint.getRegion()).isEqualTo(sourceWaypoint.getRegion());
                it.assertThat(waypoint.getCity()).isEqualTo(sourceWaypoint.getCity());
                it.assertThat(waypoint.getStreet()).isEqualTo(sourceWaypoint.getStreet());
                it.assertThat(waypoint.getHouse()).isEqualTo(sourceWaypoint.getHouse());
                it.assertThat(waypoint.getStructure()).isEqualTo(sourceWaypoint.getStructure());
                it.assertThat(waypoint.getBuilding()).isEqualTo(sourceWaypoint.getBuilding());
                it.assertThat(waypoint.getOrderingIndex()).isEqualTo(sourceWaypoint.getOrderingIndex());
                it.assertThat(waypoint.getWaitTime()).isEqualTo(sourceWaypoint.getWaitTime());
            });
        }
    }

    @Test
    @DisplayName("Проверка существования заявки")
    void test_exists() {
        final var existingId = UUID.randomUUID();
        final var nonExistingId = UUID.randomUUID();

        final var tripRequest = Instancio.of(TestTripRequest.class)
                .set(field(TestTripRequest::id), existingId)
                .set(field(TestTripRequest::waypoints), Instancio.ofList(Waypoint.class).size(3).create())
                .create();

        tripRequestsDatabaseProvider.createOrUpdate(tripRequest);

        assertSoftly(it -> {
            it.assertThat(tripRequestsDatabaseProvider.exists(existingId)).isTrue();
            it.assertThat(tripRequestsDatabaseProvider.exists(nonExistingId)).isFalse();
        });
    }

    @Test
    @DisplayName("Проверка получения полных данных заявки")
    void test_get() {
        final var tripRequestId = UUID.randomUUID();
        final var passengerId = UUID.randomUUID();
        final var approverId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        final var purposeId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        final var passengerRecord = new EmployeeRecord();
        passengerRecord.setId(passengerId);
        passengerRecord.setDepartmentId(departmentId);
        passengerRecord.setCostCenter("99LL000001");
        passengerRecord.setFirstName("Ivan");
        passengerRecord.setOrganizationId(organizationId);
        passengerRecord.setDepartmentId(departmentId);
        passengerRecord.setLastName("Ivanov");

        final var approverRecord = new EmployeeRecord();
        approverRecord.setId(approverId);
        approverRecord.setFirstName("Petr");
        approverRecord.setOrganizationId(organizationId);
        approverRecord.setDepartmentId(departmentId);
        approverRecord.setLastName("Petrov");

        final var departmentRecord = new DepartmentRecord();
        departmentRecord.setId(departmentId);
        departmentRecord.setName("IT Department");

        final var purposeRecord = new TripPurposeRecord();
        purposeRecord.setId(purposeId);
        purposeRecord.setLabel("Business Trip");

        context.executeInsert(passengerRecord);
        context.executeInsert(approverRecord);
        context.executeInsert(departmentRecord);
        context.executeInsert(purposeRecord);

        final var tripRequest = Instancio.of(TestTripRequest.class)
                .set(field(TestTripRequest::id), tripRequestId)
                .set(field(TestTripRequest::humanReadableId), "OT-0001-00014844")
                .set(field(TestTripRequest::transportType), TransportType.PUBLIC)
                .set(field(TestTripRequest::tariff), "BUSINESS")
                .set(field(TestTripRequest::passengerId), passengerId)
                .set(field(TestTripRequest::approverId), approverId)
                .set(field(TestTripRequest::desiredDate), OffsetDateTime.now())
                .set(field(TestTripRequest::approvalDate), OffsetDateTime.now())
                .set(field(TestTripRequest::plannedCost), new BigDecimal("150.75"))
                .set(field(TestTripRequest::actualCost), new BigDecimal("100.50"))
                .set(field(TestTripRequest::organizationId), organizationId)
                .set(field(TestTripRequest::departmentId), departmentId)
                .set(field(TestTripRequest::requestStatus), "NEW")
                .set(field(TestTripRequest::purposeId), purposeId)
                .set(field(TestTripRequest::timeZone), "+03:00")
                .set(field(TestTripRequest::waypoints), Instancio.ofList(Waypoint.class).size(3).create())
                .set(field(TestTripRequest::distance), 1500.0)
                .set(field(TestTripRequest::compensationType), "CITY_TRIP_COMPENSATION")
                .set(field(TestTripRequest::duration), 120L)
                .create();

        tripRequestsDatabaseProvider.createOrUpdate(tripRequest);

        createTestWaypoints(tripRequest.getId(), 3);

        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());
        fraudRecord.setRequestId(tripRequestId);
        fraudRecord.setComment("Повторная поездка");
        fraudRecord.setFraudType("UNKNOWN");
        fraudRecord.setSource("UNKNOWN");

        context.executeInsert(fraudRecord);

        final var actual = tripRequestsDatabaseProvider.get(tripRequestId);

        assertSoftly(it -> {
            it.assertThat(actual).isPresent();

            final var tripRequestData = actual.get();

            it.assertThat(tripRequestData.getId()).isEqualTo(tripRequestId);
            it.assertThat(tripRequestData.getTransportType()).isEqualTo("PUBLIC");
            it.assertThat(tripRequestData.getHumanReadableId()).isEqualTo("OT-0001-00014844");
            it.assertThat(tripRequestData.getRequestStatus()).isEqualTo("NEW");
            it.assertThat(tripRequestData.getPlannedCost()).isEqualTo(new BigDecimal("150.75"));
            it.assertThat(tripRequestData.getActualCost()).isEqualTo(new BigDecimal("100.50"));
            it.assertThat(tripRequestData.getDistance()).isEqualTo(1500.0);
            it.assertThat(tripRequestData.getCompensationType()).isEqualTo("CITY_TRIP_COMPENSATION");
            it.assertThat(tripRequestData.getApprovalDate()).isNotNull();

            it.assertThat(tripRequestData.getPassenger()).isNotNull();
            it.assertThat(tripRequestData.getPassenger().getId()).isEqualTo(passengerId);
            it.assertThat(tripRequestData.getApprover()).isNotNull();
            it.assertThat(tripRequestData.getApprover().getId()).isEqualTo(approverId);
            it.assertThat(tripRequestData.getDepartment()).isNotNull();
            it.assertThat(tripRequestData.getDepartment().getId()).isEqualTo(departmentId);
            it.assertThat(tripRequestData.getPurpose()).isNotNull();
            it.assertThat(tripRequestData.getPurpose().getId()).isEqualTo(purposeId);
            it.assertThat(tripRequestData.getCostCenter()).isEqualTo("99LL000001");

            it.assertThat(tripRequestData.getWaypoints()).hasSize(3);
            it.assertThat(tripRequestData.getWaypoints())
                    .extracting("orderingIndex")
                    .containsExactly(0, 1, 2);

            it.assertThat(tripRequestData.getFrauds()).hasSize(1);
            it.assertThat(tripRequestData.getFrauds().get(0).getComment()).isEqualTo("Повторная поездка");

            it.assertThat(tripRequestData.getDepartureAddress()).isNotNull();
            it.assertThat(tripRequestData.getDestinationAddress()).isNotNull();
            it.assertThat(tripRequestData.getDuration()).isEqualTo(120L);
        });
    }

    @Test
    @DisplayName("Проверка получения заявок с пагинацией")
    void test_get_withPagination() {
        final var passengerId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var purposeId = UUID.randomUUID();

        createTestEmployee(passengerId, departmentId, organizationId, "CC-001");
        createTestDepartment(departmentId);
        createTestPurpose(purposeId);

        for (int i = 0; i < 5; i++) {
            final var tripRequestId = UUID.randomUUID();
            final var tripRequest = new TestTripRequest(
                    tripRequestId,
                    "TRIP-" + (i + 1),
                    TransportType.PUBLIC,
                    "STANDARD",
                    passengerId,
                    UUID.randomUUID(),
                    OffsetDateTime.now().plusDays(i),
                    OffsetDateTime.now().plusDays(i).plusHours(1),
                    new BigDecimal("100.00"),
                    new BigDecimal("80.00"),
                    UUID.randomUUID(),
                    departmentId,
                    "APPROVED",
                    purposeId,
                    "+03:00",
                    Collections.emptyList(),
                    100.0,
                    "FULL",
                    120L
            );
            tripRequestsDatabaseProvider.createOrUpdate(tripRequest);

            createTestWaypoints(tripRequestId, 2);
            createTestFrauds(tripRequestId);
        }

        final var filter = new TestRequestFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        final var page = 0;
        final var size = 3;
        final var sort = "humanReadableId";
        final var asc = true;

        final var result = tripRequestsDatabaseProvider.get(filter, page, size, sort, asc);

        assertSoftly(it -> {
            it.assertThat(result).isNotNull();
            it.assertThat(result.content()).hasSize(3);
            it.assertThat(result.page().total()).isEqualTo(5);
            it.assertThat(result.page().number()).isEqualTo(0);
            it.assertThat(result.page().size()).isEqualTo(3);

            it.assertThat(result.content())
                    .extracting(TripRequestData::getHumanReadableId)
                    .containsExactly("TRIP-1", "TRIP-2", "TRIP-3");

            result.content().forEach(tripRequest -> {
                it.assertThat(tripRequest.getWaypoints()).hasSize(2);
            });

            final var firstRequest = result.content().get(0);
            it.assertThat(firstRequest.getId()).isNotNull();
            it.assertThat(firstRequest.getTransportType()).isEqualTo("PUBLIC");
            it.assertThat(firstRequest.getRequestStatus()).isEqualTo("APPROVED");
            it.assertThat(firstRequest.getPassenger()).isNotNull();
            it.assertThat(firstRequest.getPassenger().getId()).isEqualTo(passengerId);
        });
    }

    private void createTestEmployee(UUID employeeId, UUID departmentId, UUID organizationId, String costCenter) {
        final var employee = new EmployeeRecord();
        employee.setId(employeeId);
        employee.setDepartmentId(departmentId);
        employee.setOrganizationId(organizationId);
        employee.setCostCenter(costCenter);
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        context.executeInsert(employee);
    }

    private void createTestDepartment(UUID departmentId) {
        final var department = new DepartmentRecord();
        department.setId(departmentId);
        department.setName("Test Department");
        context.executeInsert(department);
    }

    private void createTestPurpose(UUID purposeId) {
        final var purpose = new TripPurposeRecord();
        purpose.setId(purposeId);
        purpose.setLabel("Test Purpose");
        context.executeInsert(purpose);
    }

    private void createTestWaypoints(UUID tripRequestId, int count) {
        for (int i = 0; i < count; i++) {
            final var waypoint = new WaypointRecord();
            waypoint.setId(UUID.randomUUID());
            waypoint.setTripRequestId(tripRequestId);
            waypoint.setOrderingIndex(i);
            waypoint.setRegion("Russia");
            waypoint.setCountry("Russia");
            waypoint.setCity("City " + i);
            waypoint.setStreet("Street " + i);
            waypoint.setHouse(String.valueOf(i + 1));
            context.executeInsert(waypoint);
        }
    }

    private void createTestFrauds(UUID tripRequestId) {
        for (int i = 0; i < 2; i++) {
            final var fraudRecord = new FraudRecord();
            fraudRecord.setId(UUID.randomUUID());
            fraudRecord.setRequestId(tripRequestId);
            fraudRecord.setComment("Fraud");
            fraudRecord.setFraudType("UNKNOWN");
            fraudRecord.setSource("UNKNOWN");
            context.executeInsert(fraudRecord);
        }
    }

    @Test
    @DisplayName("Проверка создания заявки только с ID")
    void test_createIfNotExists() {
        final var existingId = UUID.randomUUID();
        final var newId = UUID.randomUUID();

        // Создаем заявку через createOrUpdate
        final var tripRequest = Instancio.of(TestTripRequest.class)
                .set(field(TestTripRequest::id), existingId)
                .set(field(TestTripRequest::waypoints), Instancio.ofList(Waypoint.class).size(1).create())
                .create();
        tripRequestsDatabaseProvider.createOrUpdate(tripRequest);

        // Проверяем существующую заявку
        assertSoftly(it -> {
            it.assertThat(tripRequestsDatabaseProvider.createIfNotExists(existingId)).isFalse();
            it.assertThat(tripRequestsDatabaseProvider.exists(existingId)).isTrue();
        });

        // Проверяем новую заявку
        assertSoftly(it -> {
            it.assertThat(tripRequestsDatabaseProvider.createIfNotExists(newId)).isTrue();
            it.assertThat(tripRequestsDatabaseProvider.exists(newId)).isTrue();
        });

        // Проверяем, что в базе теперь 2 заявки
        assertThat(context.fetchCount(Tables.TRIP_REQUEST)).isEqualTo(2);
    }

    @Test
    @DisplayName("Проверка получения заявки с сообщениями")
    void test_getWithMessages() {
        final var tripRequestId = UUID.randomUUID();
        final var passengerId = UUID.randomUUID();
        final var approverId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        final var purposeId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        final var passengerRecord = new EmployeeRecord();
        passengerRecord.setId(passengerId);
        passengerRecord.setDepartmentId(departmentId);
        passengerRecord.setCostCenter("99LL000001");
        passengerRecord.setFirstName("Ivan");
        passengerRecord.setOrganizationId(organizationId);
        passengerRecord.setLastName("Ivanov");

        final var approverRecord = new EmployeeRecord();
        approverRecord.setId(approverId);
        approverRecord.setFirstName("Petr");
        approverRecord.setOrganizationId(organizationId);
        approverRecord.setDepartmentId(departmentId);
        approverRecord.setLastName("Petrov");

        final var departmentRecord = new DepartmentRecord();
        departmentRecord.setId(departmentId);
        departmentRecord.setName("IT Department");

        final var purposeRecord = new TripPurposeRecord();
        purposeRecord.setId(purposeId);
        purposeRecord.setLabel("Business Trip");

        context.executeInsert(passengerRecord);
        context.executeInsert(approverRecord);
        context.executeInsert(departmentRecord);
        context.executeInsert(purposeRecord);

        final var tripRequest = Instancio.of(TestTripRequest.class)
                .set(field(TestTripRequest::id), tripRequestId)
                .set(field(TestTripRequest::humanReadableId), "OT-0001-00014844")
                .set(field(TestTripRequest::transportType), TransportType.PUBLIC)
                .set(field(TestTripRequest::tariff), "BUSINESS")
                .set(field(TestTripRequest::passengerId), passengerId)
                .set(field(TestTripRequest::approverId), approverId)
                .set(field(TestTripRequest::desiredDate), OffsetDateTime.now())
                .set(field(TestTripRequest::approvalDate), OffsetDateTime.now())
                .set(field(TestTripRequest::plannedCost), new BigDecimal("150.75"))
                .set(field(TestTripRequest::actualCost), new BigDecimal("100.50"))
                .set(field(TestTripRequest::organizationId), organizationId)
                .set(field(TestTripRequest::departmentId), departmentId)
                .set(field(TestTripRequest::requestStatus), "NEW")
                .set(field(TestTripRequest::purposeId), purposeId)
                .set(field(TestTripRequest::timeZone), "+03:00")
                .set(field(TestTripRequest::waypoints), Instancio.ofList(Waypoint.class).size(3).create())
                .set(field(TestTripRequest::distance), 1500.0)
                .set(field(TestTripRequest::compensationType), "CITY_TRIP_COMPENSATION")
                .set(field(TestTripRequest::duration), 120L)
                .create();

        tripRequestsDatabaseProvider.createOrUpdate(tripRequest);

        createTestWaypoints(tripRequestId, 3);

        final var fraudId = UUID.randomUUID();
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(fraudId);
        fraudRecord.setRequestId(tripRequestId);
        fraudRecord.setComment("Повторная поездка");
        fraudRecord.setFraudType("UNKNOWN");
        fraudRecord.setSource("UNKNOWN");
        context.executeInsert(fraudRecord);

        final var messagingRecord = new MessagingRecord();
        messagingRecord.setId(UUID.randomUUID());
        messagingRecord.setFraudCaseId(fraudId);
        messagingRecord.setMessageDate(OffsetDateTime.now().toLocalDateTime());
        messagingRecord.setToEmail("testTo@test.com");
        messagingRecord.setFromEmail("testFrom@test.com");
        messagingRecord.setBody("Test message");
        context.executeInsert(messagingRecord);

        final var actual = tripRequestsDatabaseProvider.getWithMessages(tripRequestId);

        assertSoftly(it -> {
            it.assertThat(actual).isPresent();
            final var data = actual.get();
            it.assertThat(data.getId()).isEqualTo(tripRequestId);
            it.assertThat(data.getPassenger()).isNotNull();
            it.assertThat(data.getPassenger().getId()).isEqualTo(passengerId);
            it.assertThat(data.getApprover()).isNotNull();
            it.assertThat(data.getApprover().getId()).isEqualTo(approverId);
            it.assertThat(data.getDepartment()).isNotNull();
            it.assertThat(data.getDepartment().getId()).isEqualTo(departmentId);
            it.assertThat(data.getPurpose()).isNotNull();
            it.assertThat(data.getPurpose().getId()).isEqualTo(purposeId);
            it.assertThat(data.getWaypoints()).hasSize(3);
            it.assertThat(data.getFrauds()).hasSize(1);
            it.assertThat(data.getFraudMessages()).isNotEmpty();
            it.assertThat(data.getFraudMessages()).containsKey(fraudId);
            final var messages = data.getFraudMessages().get(fraudId);
            it.assertThat(messages).hasSize(1);
            it.assertThat(messages.get(0).getBody()).isEqualTo("Test message");
        });
    }

    @Test
    @DisplayName("Проверка получения заявки с сообщениями: несуществующий ID")
    void test_getWithMessages_whenNotExists_shouldReturnEmpty() {
        final var nonExistingId = UUID.randomUUID();

        final var actual = tripRequestsDatabaseProvider.getWithMessages(nonExistingId);

        assertThat(actual).isNotPresent();
    }

    private record TestRequestFilter(
            List<UUID> passenger,
            List<UUID> approver,
            String passengerName,
            String approverName,
            OffsetDateTime tripDateStart,
            OffsetDateTime tripDateEnd,
            OffsetDateTime approveDateStart,
            OffsetDateTime approveDateEnd,
            String humanReadableId,
            List<String> transportType,
            List<UUID> purpose
    ) implements RequestFilter {
    }
}