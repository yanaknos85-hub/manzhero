package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static ru.sber.transport.database.limits.Tables.SERVICES;
import static ru.sber.transport.database.limits.Tables.TYPES;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера распределений")
@ActiveProfiles("test")
class LimitSharingServiceImplTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private LimitSharingService limitSharingService;

    @Autowired
    private LimitSharingRepository limitSharingRepository;

    @Autowired
    private LimitRepository<DepLimit> limitRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DSLContext dslContext;

    @BeforeEach
    void setup() {
        dslContext.insertInto(TYPES)
                .values("TAXI")
                .execute();
        dslContext.insertInto(TYPES)
                .values("WALK")
                .execute();
        dslContext.insertInto(TYPES)
                .values("INTERREGIONAL")
                .execute();
        dslContext.insertInto(TYPES)
                .values("OFFICIAL")
                .execute();
        dslContext.insertInto(TYPES)
                .values("PUBLIC")
                .execute();
        dslContext.insertInto(TYPES)
                .values("SCOOTER")
                .execute();
        dslContext.insertInto(TYPES)
                .values("COURIER")
                .execute();
        dslContext.insertInto(TYPES)
                .values("INDIVIDUAL")
                .execute();
        dslContext.insertInto(TYPES)
                .values("BICYCLE")
                .execute();
        dslContext.insertInto(TYPES)
                .values("SPECIAL")
                .execute();
        dslContext.insertInto(TYPES)
                .values("DOMESTIC_COURIER")
                .execute();
        dslContext.insertInto(TYPES)
                .values("PERSONAL")
                .execute();
        dslContext.insertInto(TYPES)
                .values("GROUP_TRANSFER")
                .execute();
        dslContext.insertInto(TYPES)
                .values("PRIVATE")
                .execute();
        dslContext.insertInto(TYPES)
                .values("CARSHARING")
                .execute();
        dslContext.insertInto(TYPES)
                .values("DEDICATED")
                .execute();
    }

    @Test
    @DisplayName("Получение всех с фильтром. Пустой фильтр")
    void test_getAll_filtered_emptyFilter() {
        var organization = Instancio.create(Organization.class);
        organization = organizationRepository.save(organization);

        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganizationId), organization.getId())
                .create()
                ;
        department = departmentRepository.save(department);

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getDepartmentId), department.getId())
                .set(Select.field(Employee::getOrganizationId), organization.getId())
                .create();
        employee = employeeRepository.save(employee);

        var limit = Instancio.ofList(DepLimit.class)
                .size(100)
                .ignore(Select.field(DepLimit::getId))
                .ignore(Select.field(DepLimit::getParent))
                .ignore(Select.field(DepLimit::getParentDepartment))
                .set(Select.field(DepLimit::getDepartment), department)
                .set(Select.field(DepLimit::getAuthor), employee)
                .set(Select.field(DepLimit::getLimitOwner), employee)
                .set(Select.field(DepLimit::getOrganization), organization)
                .supply(Select.field(DepLimit::getLimitServiceType), () -> dslContext.insertInto(SERVICES)
                        .values(Instancio.create(String.class))
                        .returning(SERVICES.ID)
                        .fetchSingle().getId())
                .create();
        var savedLimits = limitRepository.saveAll(limit);
        var limitIndex = new AtomicInteger();

        var sharings = Instancio.ofList(LimitSharing.class)
                .size(100)
                .supply(Select.field(LimitSharing::getLimit), () -> savedLimits.get(limitIndex.getAndIncrement()))
                .set(Select.field(LimitSharing::getAuthor), employee)
                .ignore(Select.field(LimitSharing::getSharingPerPeriods))
                .ignore(Select.field(LimitSharing::getId))
                .create();
        sharings = limitSharingRepository.saveAll(sharings);
        sharings.sort(Comparator.comparing(LimitSharing::getCreationTime).reversed());

        var actualList = limitSharingService.getAll(0, 10, Sort.Direction.DESC, null);
        assertThat(actualList.getTotalElements()).isEqualTo(100);
        assertThat(actualList.getTotalPages()).isEqualTo(10);
        assertThat(actualList.getNumber()).isZero();
        assertThat(actualList.getNumberOfElements()).isEqualTo(10);
        assertThat(actualList.getSize()).isEqualTo(10);
        assertThat(actualList.getContent()).hasSize(10);

        for (var i = 0; i < actualList.getContent().size(); i++) {
            var actual = actualList.getContent().get(i);
            var expected = sharings.get(i);

            assertAll("Index %s".formatted(i),
                    () -> assertThat(actual.getLimit().getId()).isEqualTo(expected.getLimit().getId()),
                    () -> assertThat(actual.getSum()).isEqualTo(expected.getSum()),
                    () -> assertThat(actual.getBalance()).isEqualTo(expected.getBalance()),
                    () -> assertThat(actual.getCreationTime()).isEqualTo(expected.getCreationTime()),
                    () -> assertThat(actual.getId()).isEqualTo(expected.getId()),
                    () -> assertThat(actual.getTransportType()).isEqualTo(expected.getTransportType())
            );
        }
    }

    @Test
    @DisplayName("Получение всех с фильтром. Лимиты")
    void test_getAll_filtered_limits() {
        var organization = Instancio.create(Organization.class);
        organization = organizationRepository.save(organization);

        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganizationId), organization.getId())
                .create()
                ;
        department = departmentRepository.save(department);

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getDepartmentId), department.getId())
                .set(Select.field(Employee::getOrganizationId), organization.getId())
                .create();
        employee = employeeRepository.save(employee);

        var saved = organization;

        final var service = dslContext.insertInto(SERVICES)
                .values(Instancio.create(String.class))
                .returning(SERVICES.ID)
                .fetchSingle();

        var limit = Instancio.ofList(DepLimit.class)
                .size(100)
                .ignore(Select.field(DepLimit::getId))
                .ignore(Select.field(DepLimit::getParent))
                .ignore(Select.field(DepLimit::getParentDepartment))
                .supply(Select.field(DepLimit::getDepartment), () -> departmentRepository.save(Instancio.of(Department.class).set(Select.field(Department::getOrganizationId), saved.getId()).create()))
                .set(Select.field(DepLimit::getAuthor), employee)
                .set(Select.field(DepLimit::getLimitOwner), employee)
                .set(Select.field(DepLimit::getOrganization), organization)
                .set(Select.field(DepLimit::getLimitServiceType), service.getId())
                .create();
        var savedLimits = limitRepository.saveAll(limit);
        var limitIndex = new AtomicInteger();

        var sharings = Instancio.ofList(LimitSharing.class)
                .size(100)
                .supply(Select.field(LimitSharing::getLimit), () -> savedLimits.get(limitIndex.getAndIncrement()))
                .set(Select.field(LimitSharing::getAuthor), employee)
                .ignore(Select.field(LimitSharing::getSharingPerPeriods))
                .ignore(Select.field(LimitSharing::getId))
                .create();
        sharings = limitSharingRepository.saveAll(sharings);
        sharings.sort(Comparator.comparing(LimitSharing::getCreationTime));

        var actualList = limitSharingService.getAll(0, 10, Sort.Direction.DESC, sharings.getFirst().getLimit().getId());
        assertThat(actualList.getTotalElements()).isEqualTo(1);
        assertThat(actualList.getTotalPages()).isEqualTo(1);
        assertThat(actualList.getNumber()).isZero();
        assertThat(actualList.getNumberOfElements()).isEqualTo(1);
        assertThat(actualList.getSize()).isEqualTo(10);
        assertThat(actualList.getContent()).hasSize(1);

        for (var i = 0; i < 1; i++) {
            var actual = actualList.getContent().get(i);
            var expected = sharings.get(i);

            assertAll("Index %s".formatted(i),
                    () -> assertThat(actual.getLimit().getId()).isEqualTo(expected.getLimit().getId()),
                    () -> assertThat(actual.getSum()).isEqualTo(expected.getSum()),
                    () -> assertThat(actual.getBalance()).isEqualTo(expected.getBalance()),
                    () -> assertThat(actual.getCreationTime()).isEqualTo(expected.getCreationTime()),
                    () -> assertThat(actual.getId()).isEqualTo(expected.getId()),
                    () -> assertThat(actual.getTransportType()).isEqualTo(expected.getTransportType())
            );
        }
    }

}