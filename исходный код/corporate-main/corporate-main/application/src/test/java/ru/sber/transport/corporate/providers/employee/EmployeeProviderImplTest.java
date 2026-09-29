package ru.sber.transport.corporate.providers.employee;

import io.qameta.allure.Feature;
import java.time.LocalDate;
import lombok.val;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.corporate.web.model.EmployeeWebFilter;
import ru.sber.transport.database.corporate.enums.ActiveStatus;
import ru.sber.transport.database.corporate.enums.StructureType;
import ru.sber.transport.database.corporate.tables.records.DepartmentRecord;
import ru.sber.transport.database.corporate.tables.records.EmployeeRecord;
import ru.sber.transport.database.corporate.tables.records.OrganizationRecord;
import ru.sber.transport.database.corporate.tables.records.PositionRecord;
import ru.sber.transport.corporate.providers.employee.mappers.EmployeeDatabaseMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.DataCreator;
import ru.sber.transport.web.model.OrgStructureType;
import ru.sber.transport.web.model.Projection;
import ru.sberbank.ditsib.request.Direction;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.corporate.Tables.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера сотрудников")
@JooqTest(properties = "logging.level.org.jooq.tools.LoggerListener=DEBUG")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, EmployeeProviderImpl.class, EmployeeDatabaseMapperImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class EmployeeProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private EmployeeProvider provider;

    @MockitoBean
    private HumanReadableProvider<Employee> humanReadableProvider;

    private OrganizationRecord organization;

    private DepartmentRecord department;

    private PositionRecord position;

    @BeforeEach
    void setup() {
        organization = context.insertInto(ORGANIZATION).set(createOrganizationRecord(0)).returning().fetchSingle();
        department = context.insertInto(DEPARTMENT).set(createDepartmentRecord(0, organization.getId())).returning().fetchSingle();
        position = context.insertInto(POSITION).set(createPositionRecord(0, organization.getId())).returning().fetchSingle();
    }

    @SuppressWarnings("java:S5961")
    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var source = Instancio.of(Employee.class)
                .set(Select.field(Employee::getOrganizationId), organization.getId())
                .set(Select.field(Employee::getDepartmentId), department.getId())
                .set(Select.field(Employee::getPositionId), position.getId())
                .ignore(Select.field(Employee::getSupervisorId))
                .create();

        var saved = provider.save(source);

        assertThat(context.fetchCount(context.selectFrom(EMPLOYEE))).isEqualTo(1);

        var actual = context.selectFrom(EMPLOYEE).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(source.getId());
            it.assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(actual.getConsent()).isEqualTo(source.isConsent());
            it.assertThat(actual.getCostCenter()).isEqualTo(source.getCostCenter());
            it.assertThat(actual.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(actual.getHumanreadableid()).isEqualTo(source.getHumanReadableId());
            it.assertThat(actual.getEmail()).isEqualTo(source.getEmail());
            it.assertThat(actual.getExternalEmail()).isEqualTo(source.getExternalEmail());
            it.assertThat(actual.getFirstName()).isEqualTo(source.getFirstName());
            it.assertThat(actual.getHumanreadableid()).isEqualTo(source.getHumanReadableId());
            it.assertThat(actual.getLastName()).isEqualTo(source.getLastName());
            it.assertThat(actual.getMarriageCertificateId()).isEqualTo(source.getMarriageCertificate());
            it.assertThat(actual.getFireDate()).isEqualTo(source.getFireDate());
            it.assertThat(actual.getMobilePhone()).isEqualTo(source.getPhone());
            it.assertThat(actual.getPatronymic()).isEqualTo(source.getPatronymic());
            it.assertThat(actual.getPersonnelNumber()).isEqualTo(source.getPersonnelNumber());
            it.assertThat(actual.getRoom()).isEqualTo(source.getRoom());
        });

        assertSoftly(it -> {
            it.assertThat(saved.getId()).isEqualTo(source.getId());
            it.assertThat(saved.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(saved.isConsent()).isEqualTo(source.isConsent());
            it.assertThat(saved.getCostCenter()).isEqualTo(source.getCostCenter());
            it.assertThat(saved.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(saved.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(saved.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
            it.assertThat(saved.getEmail()).isEqualTo(source.getEmail());
            it.assertThat(saved.getExternalEmail()).isEqualTo(source.getExternalEmail());
            it.assertThat(saved.getFirstName()).isEqualTo(source.getFirstName());
            it.assertThat(saved.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
            it.assertThat(saved.getLastName()).isEqualTo(source.getLastName());
            it.assertThat(saved.getMarriageCertificate()).isEqualTo(source.getMarriageCertificate());
            it.assertThat(saved.getFireDate()).isEqualTo(source.getFireDate());
            it.assertThat(saved.getPhone()).isEqualTo(source.getPhone());
            it.assertThat(saved.getPatronymic()).isEqualTo(source.getPatronymic());
            it.assertThat(saved.getPersonnelNumber()).isEqualTo(source.getPersonnelNumber());
            it.assertThat(saved.getRoom()).isEqualTo(source.getRoom());
        });
    }

    @Test
    @DisplayName("Получение всех")
    void test_get_all() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createEmployeeRecord(it, organization.getId(), department.getId(), position.getId()))
                .map(it -> context.insertInto(EMPLOYEE).set(it).returning().fetchSingle())
                .toList();

        var actualList = provider.get();

        assertThat(actualList).hasSameSizeAs(expected);
    }

    @Test
    @DisplayName("Получение всех по организациям и ТН")
    void test_get_all_by_organizations() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createEmployeeRecord(it, organization.getId(), department.getId(), position.getId()))
                .map(it -> context.insertInto(EMPLOYEE).set(it).returning().fetchSingle())
                .toList();

        var data = expected.parallelStream().map(it -> new AbstractMap.SimpleEntry<>(it.getOrganizationId(), it.getPersonnelNumber())).collect(Collectors.<Map.Entry<UUID, String>>toUnmodifiableSet());
        var actualList = provider.get(data);

        assertThat(actualList).hasSameSizeAs(expected);
    }

    @Test
    @DisplayName("Получение по организациям и хэд ID")
    void test_get_by_organizations_and_head() {
        var expected = IntStream.range(0, 10)
            .mapToObj(it -> createEmployeeRecord(it, organization.getId(), department.getId(), position.getId()))
            .map(it -> context.insertInto(EMPLOYEE).set(it).returning().fetchSingle())
            .toList();

        var actual = provider.get(organization.getId(), expected.get(5).getPersonnelNumber());

        assertThat(actual).isPresent();

        assertSoftly(it -> {
            it.assertThat(actual.get().getId()).isEqualTo(expected.get(5).getId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(5).getStatus().name());
            it.assertThat(actual.get().isConsent()).isEqualTo(expected.get(5).getConsent());
            it.assertThat(actual.get().getCostCenter()).isEqualTo(expected.get(5).getCostCenter());
            it.assertThat(actual.get().getOrganizationId()).isEqualTo(expected.get(5).getOrganizationId());
            it.assertThat(actual.get().getHumanReadableId()).isEqualTo(expected.get(5).getHumanreadableid());
            it.assertThat(actual.get().getEmail()).isEqualTo(expected.get(5).getEmail());
            it.assertThat(actual.get().getExternalEmail()).isEqualTo(expected.get(5).getExternalEmail());
            it.assertThat(actual.get().getFirstName()).isEqualTo(expected.get(5).getFirstName());
            it.assertThat(actual.get().getLastName()).isEqualTo(expected.get(5).getLastName());
            it.assertThat(actual.get().getMarriageCertificate()).isEqualTo(expected.get(5).getMarriageCertificateId());
            it.assertThat(actual.get().getFireDate()).isEqualTo(expected.get(5).getFireDate());
            it.assertThat(actual.get().getPhone()).isEqualTo(expected.get(5).getMobilePhone());
            it.assertThat(actual.get().getPatronymic()).isEqualTo(expected.get(5).getPatronymic());
            it.assertThat(actual.get().getPersonnelNumber()).isEqualTo(expected.get(5).getPersonnelNumber());
        });
    }

    @Test
    @DisplayName("Получение одной записи")
    void test_get() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createEmployeeRecord(it, organization.getId(), department.getId(), position.getId()))
                .map(it -> context.insertInto(EMPLOYEE).set(it).returning().fetchSingle())
                .toList();

        var actual = provider.get(expected.get(10).getId());

        assertThat(actual).isPresent();

        assertSoftly(it -> {
            it.assertThat(actual.get().getId()).isEqualTo(expected.get(10).getId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().isConsent()).isEqualTo(expected.get(10).getConsent());
            it.assertThat(actual.get().getCostCenter()).isEqualTo(expected.get(10).getCostCenter());
            it.assertThat(actual.get().getOrganizationId()).isEqualTo(expected.get(10).getOrganizationId());
            it.assertThat(actual.get().getHumanReadableId()).isEqualTo(expected.get(10).getHumanreadableid());
            it.assertThat(actual.get().getEmail()).isEqualTo(expected.get(10).getEmail());
            it.assertThat(actual.get().getExternalEmail()).isEqualTo(expected.get(10).getExternalEmail());
            it.assertThat(actual.get().getFirstName()).isEqualTo(expected.get(10).getFirstName());
            it.assertThat(actual.get().getLastName()).isEqualTo(expected.get(10).getLastName());
            it.assertThat(actual.get().getMarriageCertificate()).isEqualTo(expected.get(10).getMarriageCertificateId());
            it.assertThat(actual.get().getFireDate()).isEqualTo(expected.get(10).getFireDate());
            it.assertThat(actual.get().getPhone()).isEqualTo(expected.get(10).getMobilePhone());
            it.assertThat(actual.get().getPatronymic()).isEqualTo(expected.get(10).getPatronymic());
            it.assertThat(actual.get().getPersonnelNumber()).isEqualTo(expected.get(10).getPersonnelNumber());
            it.assertThat(actual.get().getRoom()).isEqualTo(expected.get(10).getRoom());
        });
    }

    @Test
    @DisplayName("Получение одной записи по ID")
    void givenId_whenGetByIdOrPersonalNumber_thenSuccess() {
        var expected = IntStream.range(0, 100)
            .mapToObj(it -> createEmployeeRecord(it, organization.getId(), department.getId(), position.getId()))
            .map(it -> context.insertInto(EMPLOYEE).set(it).returning().fetchSingle())
            .toList();

        var actual = provider.getByIdOrPersonalNumber(expected.get(10).getId(), null);

        assertThat(actual).isPresent();

        assertSoftly(it -> {
            it.assertThat(actual.get().getId()).isEqualTo(expected.get(10).getId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().isConsent()).isEqualTo(expected.get(10).getConsent());
            it.assertThat(actual.get().getCostCenter()).isEqualTo(expected.get(10).getCostCenter());
            it.assertThat(actual.get().getOrganizationId()).isEqualTo(expected.get(10).getOrganizationId());
            it.assertThat(actual.get().getHumanReadableId()).isEqualTo(expected.get(10).getHumanreadableid());
            it.assertThat(actual.get().getEmail()).isEqualTo(expected.get(10).getEmail());
            it.assertThat(actual.get().getExternalEmail()).isEqualTo(expected.get(10).getExternalEmail());
            it.assertThat(actual.get().getFirstName()).isEqualTo(expected.get(10).getFirstName());
            it.assertThat(actual.get().getLastName()).isEqualTo(expected.get(10).getLastName());
            it.assertThat(actual.get().getMarriageCertificate()).isEqualTo(expected.get(10).getMarriageCertificateId());
            it.assertThat(actual.get().getFireDate()).isEqualTo(expected.get(10).getFireDate());
            it.assertThat(actual.get().getPhone()).isEqualTo(expected.get(10).getMobilePhone());
            it.assertThat(actual.get().getPatronymic()).isEqualTo(expected.get(10).getPatronymic());
            it.assertThat(actual.get().getPersonnelNumber()).isEqualTo(expected.get(10).getPersonnelNumber());
            it.assertThat(actual.get().getRoom()).isEqualTo(expected.get(10).getRoom());
        });
    }

    @Test
    @DisplayName("Получение одной записи по ТН")
    void givenPersonalNumber_whenGetByIdOrPersonalNumber_thenSuccess() {
        var expected = IntStream.range(0, 100)
            .mapToObj(it -> createEmployeeRecord(it, organization.getId(), department.getId(), position.getId()))
            .map(it -> context.insertInto(EMPLOYEE).set(it).returning().fetchSingle())
            .toList();

        var actual = provider.getByIdOrPersonalNumber(null, expected.get(10).getPersonnelNumber());

        assertThat(actual).isPresent();

        assertSoftly(it -> {
            it.assertThat(actual.get().getId()).isEqualTo(expected.get(10).getId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().isConsent()).isEqualTo(expected.get(10).getConsent());
            it.assertThat(actual.get().getCostCenter()).isEqualTo(expected.get(10).getCostCenter());
            it.assertThat(actual.get().getOrganizationId()).isEqualTo(expected.get(10).getOrganizationId());
            it.assertThat(actual.get().getHumanReadableId()).isEqualTo(expected.get(10).getHumanreadableid());
            it.assertThat(actual.get().getEmail()).isEqualTo(expected.get(10).getEmail());
            it.assertThat(actual.get().getExternalEmail()).isEqualTo(expected.get(10).getExternalEmail());
            it.assertThat(actual.get().getFirstName()).isEqualTo(expected.get(10).getFirstName());
            it.assertThat(actual.get().getLastName()).isEqualTo(expected.get(10).getLastName());
            it.assertThat(actual.get().getMarriageCertificate()).isEqualTo(expected.get(10).getMarriageCertificateId());
            it.assertThat(actual.get().getFireDate()).isEqualTo(expected.get(10).getFireDate());
            it.assertThat(actual.get().getPhone()).isEqualTo(expected.get(10).getMobilePhone());
            it.assertThat(actual.get().getPatronymic()).isEqualTo(expected.get(10).getPatronymic());
            it.assertThat(actual.get().getPersonnelNumber()).isEqualTo(expected.get(10).getPersonnelNumber());
            it.assertThat(actual.get().getRoom()).isEqualTo(expected.get(10).getRoom());
        });
    }

    @Test
    @DisplayName("Получение одной записи по ТН и ID null")
    void givenPersonalNumber_whenGetByIdOrPersonalNumber_thenUnsupportedOperationException() {
        val organization2 = context.insertInto(ORGANIZATION).set(createOrganizationRecord(1)).returning().fetchSingle();
        val department2 = context.insertInto(DEPARTMENT).set(createDepartmentRecord(1, organization2.getId())).returning().fetchSingle();
        val position2 = context.insertInto(POSITION).set(createPositionRecord(1, organization2.getId())).returning().fetchSingle();
        val employee1 = context.insertInto(EMPLOYEE).set(createEmployeeRecord(1, organization.getId(), department.getId(), position.getId())).returning().fetchSingle();

        var employeeRecord = new EmployeeRecord();
        employeeRecord.setId(UUID.randomUUID());
        employeeRecord.setStatus(ActiveStatus.values()[0 % ActiveStatus.values().length]);
        employeeRecord.setConsent(true);
        employeeRecord.setCostCenter("Cost center " + 0);
        employeeRecord.setOrganizationId(organization2.getId());
        employeeRecord.setHumanreadableid("HRI " + 0);
        employeeRecord.setEmail("Email " + 0);
        employeeRecord.setExternalEmail("External email " + 0);
        employeeRecord.setFirstName("First name " + 0);
        employeeRecord.setLastName("Last name " + 0);
        employeeRecord.setMarriageCertificateId("Marriage " + 0);
        employeeRecord.setFireDate(LocalDate.now());
        employeeRecord.setMobilePhone("Phone " + 0);
        employeeRecord.setPatronymic("Patronymic " + 0);
        employeeRecord.setPersonnelNumber("Personnel number " + 1);
        employeeRecord.setRoom("Room " + 0);
        employeeRecord.setDepartmentId(department2.getId());
        employeeRecord.setPositionId(position2.getId());
        employeeRecord.setOrgStructureType(StructureType.values()[0 % StructureType.values().length]);
        context.insertInto(EMPLOYEE).set(employeeRecord).returning().fetchSingle();

        assertThrows(UnsupportedOperationException.class, () -> provider.getByIdOrPersonalNumber(null, employee1.getPersonnelNumber()));
    }

    @Test
    @DisplayName("Сохранение всех записей")
    void test_save_all() {
        var data = Instancio.ofList(Employee.class)
                .ignore(Select.field(Employee::getHumanReadableId))
                .ignore(Select.field(Employee::getSupervisorId))
                .set(Select.field(Employee::getOrganizationId), organization.getId())
                .set(Select.field(Employee::getDepartmentId), department.getId())
                .set(Select.field(Employee::getPositionId), position.getId())
                .create();

        when(humanReadableProvider.getNext(organization.getId(), data.size())).thenReturn(IntStream.range(0, data.size()).boxed().map(String::valueOf).toList());

        provider.saveAll(data);

        var actual = context.selectFrom(EMPLOYEE).orderBy(EMPLOYEE.HUMANREADABLEID).fetchInto(EmployeeRecord.class);

        assertThat(actual).hasSameSizeAs(data);

        for (var i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getHumanreadableid()).endsWith(String.valueOf(i));
        }
    }

    @Test
    @DisplayName("Получение всех. Фильтр. Полный")
    void test_get_all_filtered() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createEmployeeRecord(it, organization.getId(), department.getId(), position.getId()))
                .map(it -> context.insertInto(EMPLOYEE).set(it).returning().fetchSingle())
                .toList();

        final var filter = EmployeeWebFilter
                .builder()
                .email(expected.getFirst().getEmail())
                .build();
        var actualList = provider.streamAll(filter, Projection.FULL, 0, 100, "lastName", Direction.DESC);

        final var actual = actualList.getContent().getFirst();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(expected.getFirst().getId());
            it.assertThat(actual.getStatus().name()).isEqualTo(expected.getFirst().getStatus().name());
            it.assertThat(actual.isConsent()).isEqualTo(expected.getFirst().getConsent());
            it.assertThat(actual.getCostCenter()).isEqualTo(expected.getFirst().getCostCenter());
            it.assertThat(actual.getOrganizationId()).isEqualTo(expected.getFirst().getOrganizationId());
            it.assertThat(actual.getDepartmentId()).isEqualTo(expected.getFirst().getDepartmentId());
            it.assertThat(actual.getStatus().name()).isEqualTo(expected.getFirst().getStatus().name());
            it.assertThat(actual.getLastName()).isEqualTo(expected.getFirst().getLastName());
            it.assertThat(actual.getFirstName()).isEqualTo(expected.getFirst().getFirstName());
            it.assertThat(actual.getPatronymic()).isEqualTo(expected.getFirst().getPatronymic());
            it.assertThat(actual.getPersonnelNumber()).isEqualTo(expected.getFirst().getPersonnelNumber());
        });
    }

    @Test
    @DisplayName("Получение всех. Фильтр. Минимальный")
    void test_get_all_filtered_min() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createEmployeeRecord(it, organization.getId(), department.getId(), position.getId()))
                .map(it -> context.insertInto(EMPLOYEE).set(it).returning().fetchSingle())
                .toList();

        final var filter = EmployeeWebFilter
                .builder()
                .email(expected.getFirst().getEmail())
                .status(Active.valueOf(expected.getFirst().getStatus().name()))
                .fullName(expected.getFirst().getLastName() + " " + expected.getFirst().getFirstName())
                .mobilePhone(expected.getFirst().getMobilePhone())
                .personnelNumber(expected.getFirst().getPersonnelNumber())
                .humanReadableId(expected.getFirst().getHumanreadableid())
                .employees(List.of(expected.getFirst().getId()))
                .organizations(List.of(expected.getFirst().getOrganizationId()))
                .departments(List.of(expected.getFirst().getDepartmentId()))
                .orgStructureType(OrgStructureType.INTERNAL)
                .build();
        var actualList = provider.streamAll(filter, Projection.MIN, 0, 100, "lastName", Direction.DESC);

        final var actual = actualList.getContent().getFirst();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(expected.getFirst().getId());
            it.assertThat(actual.getLastName()).isEqualTo(expected.getFirst().getLastName());
            it.assertThat(actual.getFirstName()).isEqualTo(expected.getFirst().getFirstName());
            it.assertThat(actual.getPatronymic()).isEqualTo(expected.getFirst().getPatronymic());
            it.assertThat(actual.getPersonnelNumber()).isEqualTo(expected.getFirst().getPersonnelNumber());
        });
    }

}