package ru.sber.transport.corporate.providers.department;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.database.corporate.tables.records.DepartmentRecord;
import ru.sber.transport.database.corporate.tables.records.OrganizationRecord;
import ru.sber.transport.corporate.providers.department.mappers.DepartmentDatabaseMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.DataCreator;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.corporate.Tables.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка провайдера организаций")
@JooqTest(properties = "logging.level.org.jooq.tools.LoggerListener=DEBUG")
@ContextConfiguration(classes = {JooqDatabaseConfig.class, DepartmentProviderImpl.class, DepartmentDatabaseMapperImpl.class})
@ActiveProfiles("test")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class DepartmentProviderImplTest implements DataCreator {

    @Autowired
    private DSLContext context;

    @Autowired
    private DepartmentProvider provider;

    @MockitoBean
    private HumanReadableProvider<Department> humanReadableProvider;

    private OrganizationRecord organization;

    @BeforeEach
    void setup() {
        organization = context.insertInto(ORGANIZATION).set(createOrganizationRecord(0)).returning().fetchSingle();
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var source = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganizationId), organization.getId())
                .ignore(Select.field(Department::getHeadId))
                .ignore(Select.field(Department::getParentId))
                .set(Select.field(Department::getLevelCode), Instancio.create(Integer.class).toString())
                .create();

        var saved = provider.save(source);

        assertThat(context.fetchCount(context.selectFrom(DEPARTMENT)))
                .isEqualTo(1);

        var actual = context.selectFrom(DEPARTMENT).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(source.getId());
            it.assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(actual.getName()).isEqualTo(source.getName());
            it.assertThat(actual.getCode()).isEqualTo(source.getCode());
            it.assertThat(actual.getSyncId()).isEqualTo(source.getSyncId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(actual.getHumanreadableid()).isEqualTo(source.getHumanReadableId());
            it.assertThat(actual.getParentId()).isEqualTo(source.getParentId());
        });

        assertSoftly(it -> {
            it.assertThat(saved.getId()).isEqualTo(source.getId());
            it.assertThat(saved.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(saved.getName()).isEqualTo(source.getName());
            it.assertThat(saved.getCode()).isEqualTo(source.getCode());
            it.assertThat(saved.getSyncId()).isEqualTo(source.getSyncId());
            it.assertThat(saved.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(saved.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(saved.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
            it.assertThat(saved.getParentId()).isEqualTo(source.getParentId());
        });
    }

    @Test
    @DisplayName("Проверка сохранения при синхронизации")
    void test_save_sync() {
        var source = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganizationId), organization.getId())
                .ignore(Select.field(Department::getHeadId))
                .ignore(Select.field(Department::getParentId))
                .set(Select.field(Department::getLevelCode), Instancio.create(Integer.class).toString())
                .create();

        var departmentRecord = createDepartmentRecord(0, organization.getId());
        departmentRecord.setSyncId(source.getSyncId());
        departmentRecord.setOrganizationId(source.getOrganizationId());

        context.insertInto(DEPARTMENT)
                .set(departmentRecord)
                .execute();

        provider.saveSync(source);

        assertThat(context.fetchCount(context.selectFrom(DEPARTMENT)))
                .isEqualTo(1);

        var actual = context.selectFrom(DEPARTMENT).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(departmentRecord.getId());
            it.assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(actual.getName()).isEqualTo(source.getName());
            it.assertThat(actual.getCode()).isEqualTo(source.getCode());
            it.assertThat(actual.getSyncId()).isEqualTo(source.getSyncId());
            it.assertThat(actual.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
            it.assertThat(actual.getHumanreadableid()).isEqualTo(source.getHumanReadableId());
            it.assertThat(actual.getParentId()).isEqualTo(source.getParentId());
        });
    }

    @Test
    @DisplayName("Получение всех")
    void test_get_all() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createDepartmentRecord(it, organization.getId()))
                .map(it -> context.insertInto(DEPARTMENT).set(it).returning().fetchSingle())
                .toList();

        var actualList = provider.get();

        assertThat(actualList).hasSameSizeAs(expected);
    }

    @Test
    @DisplayName("Получение одной записи")
    void test_get() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createDepartmentRecord(it, organization.getId()))
                .map(it -> context.insertInto(DEPARTMENT).set(it).returning().fetchSingle())
                .toList();

        var actual = provider.get(expected.get(10).getId());

        assertThat(actual).isPresent();

        assertSoftly(it -> {
            it.assertThat(actual.get().getId()).isEqualTo(expected.get(10).getId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().getName()).isEqualTo(expected.get(10).getName());
            it.assertThat(actual.get().getCode()).isEqualTo(expected.get(10).getCode());
            it.assertThat(actual.get().getSyncId()).isEqualTo(expected.get(10).getSyncId());
            it.assertThat(actual.get().getOrganizationId()).isEqualTo(expected.get(10).getOrganizationId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().getHumanReadableId()).isEqualTo(expected.get(10).getHumanreadableid());
            it.assertThat(actual.get().getParentId()).isEqualTo(expected.get(10).getParentId());
        });
    }

    @Test
    @DisplayName("Получение одной записи по идентификатору синхронизации и организации")
    void test_get_byOrganizationAndSyncId() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createDepartmentRecord(it, organization.getId()))
                .map(it -> context.insertInto(DEPARTMENT).set(it).returning().fetchSingle())
                .toList();

        var departmentRecord = expected.get(10);
        var actual = provider.get(departmentRecord.getOrganizationId(), departmentRecord.getSyncId());

        assertThat(actual).isPresent();

        assertSoftly(it -> {
            it.assertThat(actual.get().getId()).isEqualTo(expected.get(10).getId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().getName()).isEqualTo(expected.get(10).getName());
            it.assertThat(actual.get().getCode()).isEqualTo(expected.get(10).getCode());
            it.assertThat(actual.get().getSyncId()).isEqualTo(expected.get(10).getSyncId());
            it.assertThat(actual.get().getOrganizationId()).isEqualTo(expected.get(10).getOrganizationId());
            it.assertThat(actual.get().getStatus().name()).isEqualTo(expected.get(10).getStatus().name());
            it.assertThat(actual.get().getHumanReadableId()).isEqualTo(expected.get(10).getHumanreadableid());
            it.assertThat(actual.get().getParentId()).isEqualTo(expected.get(10).getParentId());
        });
    }

    @Test
    @DisplayName("Поиск подчиненных подразделений")
    void test_findByHead() {
        var department = context.insertInto(DEPARTMENT).set(createDepartmentRecord(0, organization.getId())).returning().fetchSingle();
        var position = context.insertInto(POSITION).set(createPositionRecord(0, organization.getId())).returning().fetchSingle();
        var employee = context.insertInto(EMPLOYEE).set(createEmployeeRecord(0, organization.getId(), department.getId(), position.getId())).returning().fetchSingle();
        context.update(DEPARTMENT).set(DEPARTMENT.HEAD, employee.getId()).execute();

        var departments = provider.findOfHead(employee.getId());

        assertThat(departments).hasSize(1);

        assertSoftly(it -> it.assertThat(departments.getFirst()).isEqualTo(department.getId()));
    }

    @Test
    @DisplayName("Сохранение всех записей")
    void test_save_all() {
        var data = Instancio.ofList(Department.class)
                .ignore(Select.field(Department::getHumanReadableId))
                .ignore(Select.field(Department::getHeadId))
                .ignore(Select.field(Department::getLevelCode))
                .ignore(Select.field(Department::getParentId))
                .set(Select.field(Department::getOrganizationId), organization.getId())
                .create();

        when(humanReadableProvider.getNext(organization.getId(), data.size())).thenReturn(IntStream.range(0, data.size()).boxed().map(String::valueOf).toList());

        provider.saveAll(data);

        var actual = context.selectFrom(DEPARTMENT).orderBy(DEPARTMENT.HUMANREADABLEID).fetchInto(DepartmentRecord.class);

        assertThat(actual).hasSameSizeAs(data);

        for (var i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getHumanreadableid()).endsWith(String.valueOf(i));
        }
    }

    @Test
    @DisplayName("Получение организаций")
    void test_getOrganizations() {
        var expected = IntStream.range(0, 100)
                .mapToObj(it -> createDepartmentRecord(it, organization.getId()))
                .map(it -> context.insertInto(DEPARTMENT).set(it).returning().fetchSingle())
                .toList();

        var organizations = provider.getOrganizations(expected.parallelStream().map(DepartmentRecord::getId).toList());

        for (var entry : organizations.entrySet()) {
            assertThat(organizations).containsEntry(entry.getKey(), entry.getValue());
        }
    }

    @Test
    @DisplayName("Обработка подразделений без руководителей")
    void test_handleHeadlesses() {
        final var dep1 = context.insertInto(DEPARTMENT).set(createDepartmentRecord(0, organization.getId())).returning().fetchSingle();
        final var dep2 = context.insertInto(DEPARTMENT).set(createDepartmentRecord(1, organization.getId(), dep1.getId())).returning().fetchSingle();
        final var dep3 = context.insertInto(DEPARTMENT).set(createDepartmentRecord(2, organization.getId(), dep2.getId())).returning().fetchSingle();
        final var dep4 = context.insertInto(DEPARTMENT).set(createDepartmentRecord(3, organization.getId(), dep3.getId())).returning().fetchSingle();
        final var dep5 = context.insertInto(DEPARTMENT).set(createDepartmentRecord(4, organization.getId(), dep4.getId())).returning().fetchSingle();

        final var position = context.insertInto(POSITION).set(createPositionRecord(0, organization.getId())).returning().fetchSingle();
        final var employee1 = context.insertInto(EMPLOYEE).set(createEmployeeRecord(1, organization.getId(), dep2.getId(), position.getId())).returning().fetchSingle();
        final var employee2 = context.insertInto(EMPLOYEE).set(createEmployeeRecord(2, organization.getId(), dep4.getId(), position.getId())).returning().fetchSingle();

        context.update(DEPARTMENT).set(DEPARTMENT.HEAD, employee1.getId()).where(DEPARTMENT.ID.eq(dep2.getId())).execute();
        context.update(DEPARTMENT).set(DEPARTMENT.HEAD, employee2.getId()).where(DEPARTMENT.ID.eq(dep4.getId())).execute();

        assertThat(provider.getHeadlessDepartments(organization.getId())).containsExactly(dep1.getId(), dep3.getId(), dep5.getId());

        provider.fillHeads(organization.getId());

        assertThat(provider.getHeadlessDepartments(organization.getId())).containsExactly(dep1.getId());

        final var actualList = context.selectFrom(DEPARTMENT).orderBy(DEPARTMENT.HUMANREADABLEID).fetchStream().toList();
        assertThat(actualList.get(0).getHead()).isNull();
        assertThat(actualList.get(1).getHead()).isEqualTo(employee1.getId());
        assertThat(actualList.get(2).getHead()).isEqualTo(employee1.getId());
        assertThat(actualList.get(3).getHead()).isEqualTo(employee2.getId());
        assertThat(actualList.get(4).getHead()).isEqualTo(employee2.getId());
    }
}