package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTypeExcludeFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.LimitSpendingService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.database.limits.Tables.SERVICES;
import static ru.sber.transport.database.limits.Tables.TYPES;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка сервиса трат")
@TypeExcludeFilters({DataJpaTypeExcludeFilter.class})
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestEntityManager
@ImportAutoConfiguration
@JooqTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@TestPropertySource(properties = "spring.main.lazy-initialization=true")
class LimitSpendingServiceImplTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepLimitRepository depLimitRepository;

    @Autowired
    private LimitSharingRepository limitSharingRepository;

    @Autowired
    private LimitSharingPerPeriodRepository limitSharingPerPeriodRepository;

    @Autowired
    private LimitSpendingRepository spendingRepository;

    @Autowired
    private LimitSpendingService service;

    @Autowired
    private DSLContext dslContext;

    @BeforeEach
    void init() {
        dslContext.insertInto(TYPES)
                .values("PERSONAL")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("PUBLIC")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("CARSHARING")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("GROUP_TRANSFER")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("TAXI")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("INTERREGIONAL")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("DEDICATED")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("BICYCLE")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("WALK")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("SCOOTER")
                .onConflictDoNothing().execute();
    }

    @Test
    @DisplayName("Получение периодов трат")
    void test_getSpendingPeriods() {
        var employees = Instancio.ofList(Employee.class)
                .create();
        var savedEmployees = employeeRepository.saveAll(employees);
        var organizations = Instancio.ofList(Organization.class)
                .create();
        var savedOrganizations = organizationRepository.saveAll(organizations);

        final var serviceType = dslContext.insertInto(SERVICES)
                .values(Instancio.create(String.class))
                .returning(SERVICES.ID)
                .fetchSingle();

        var depLimits = Instancio.ofList(DepLimit.class)
                .ignore(Select.field(DepLimit::getId))
                .ignore(Select.field(DepLimit::getDepartment))
                .ignore(Select.field(DepLimit::getParentDepartment))
                .generate(Select.field(DepLimit::getAuthor), gen -> gen.oneOf(savedEmployees))
                .generate(Select.field(DepLimit::getLimitOwner), gen -> gen.oneOf(savedEmployees))
                .generate(Select.field(DepLimit::getOrganization), gen -> gen.oneOf(savedOrganizations))
                .set(Select.field(DepLimit::getLimitServiceType), serviceType.getId())
                .create();
        var savedDepLimits = depLimitRepository.saveAll(depLimits);

        var transportTypeOrdinal = new AtomicInteger();
        var sharings = Instancio.ofList(LimitSharing.class)
                .ignore(Select.field(LimitSharing::getId))
                .generate(Select.field(LimitSharing::getLimit), gen -> gen.oneOf(savedDepLimits))
                .generate(Select.field(LimitSharing::getAuthor), gen -> gen.oneOf(savedEmployees))
                .supply(Select.field(LimitSharing::getTransportType), () -> TransportTypeEnum.values()[transportTypeOrdinal.getAndIncrement()])
                .create();
        var savedSharings = limitSharingRepository.saveAll(sharings);

        var monthOrdinal = new AtomicInteger();
        var sharingsPerPeriod = Instancio.ofList(LimitSharingPerPeriod.class)
                .ignore(Select.field(LimitSharingPerPeriod::getId))
                .generate(Select.field(LimitSharingPerPeriod::getLimitSharing), gen -> gen.oneOf(savedSharings))
                .generate(Select.field(LimitSharingPerPeriod::getAuthor), gen -> gen.oneOf(savedEmployees))
                .create()
                .stream().peek(it -> it.setPeriodData(PeriodData.values()[monthOrdinal.incrementAndGet()])).toList();
        var savedSharingsPerPeriod = limitSharingPerPeriodRepository.saveAll(sharingsPerPeriod);

        var spendings = Instancio.ofList(LimitSpending.class)
                .ignore(Select.field(LimitSpending::getId))
                .generate(Select.field(LimitSpending::getLimitSharingPerPeriod), gen -> gen.oneOf(savedSharingsPerPeriod))
                .generate(Select.field(LimitSpending::getEmployee), gen -> gen.oneOf(savedEmployees))
                .create();
        var savedSpendings = spendingRepository.saveAll(spendings);

        var actualMap = service.getSpendingPeriods(savedSpendings.parallelStream().map(LimitSpending::getId).toList());

        assertThat(actualMap).hasSameSizeAs(savedSpendings);
        assertThat(actualMap.keySet()).hasSameElementsAs(savedSpendings.parallelStream().map(LimitSpending::getId).toList());
        assertThat(actualMap.values()).hasSameElementsAs(ReflectionUtils.cast(savedSpendings.stream().map(LimitSpending::getLimitSharingPerPeriod).map(LimitSharingPerPeriod::getPeriod).toList()));
    }

    @Test
    @DisplayName("Получение распределений")
    void test_getSpendingSharings() {
        var employees = Instancio.ofList(Employee.class)
                .create();
        var savedEmployees = employeeRepository.saveAll(employees);
        var organizations = Instancio.ofList(Organization.class)
                .create();
        var savedOrganizations = organizationRepository.saveAll(organizations);

        final var service = dslContext.insertInto(SERVICES)
                .values(Instancio.create(String.class))
                .returning(SERVICES.ID)
                .fetchSingle();

        var depLimits = Instancio.ofList(DepLimit.class)
                .ignore(Select.field(DepLimit::getId))
                .ignore(Select.field(DepLimit::getDepartment))
                .ignore(Select.field(DepLimit::getParentDepartment))
                .generate(Select.field(DepLimit::getAuthor), gen -> gen.oneOf(savedEmployees))
                .generate(Select.field(DepLimit::getLimitOwner), gen -> gen.oneOf(savedEmployees))
                .generate(Select.field(DepLimit::getOrganization), gen -> gen.oneOf(savedOrganizations))
                .set(Select.field(DepLimit::getLimitServiceType), service.getId())
                .create();
        var savedDepLimits = depLimitRepository.saveAll(depLimits);

        var type = new AtomicInteger();
        var sharings = Instancio.ofList(LimitSharing.class)
                .size(5)
                .ignore(Select.field(LimitSharing::getId))
                .supply(Select.field(LimitSharing::getTransportType), () -> TransportTypeEnum.values()[type.getAndIncrement()])
                .generate(Select.field(LimitSharing::getLimit), gen -> gen.oneOf(savedDepLimits))
                .generate(Select.field(LimitSharing::getAuthor), gen -> gen.oneOf(savedEmployees))
                .create();
        var savedSharings = limitSharingRepository.saveAll(sharings);

        var monthOrdinal = new AtomicInteger();
        var sharingsPerPeriod = Instancio.ofList(LimitSharingPerPeriod.class)
                .ignore(Select.field(LimitSharingPerPeriod::getId))
                .generate(Select.field(LimitSharingPerPeriod::getLimitSharing), gen -> gen.oneOf(savedSharings))
                .generate(Select.field(LimitSharingPerPeriod::getAuthor), gen -> gen.oneOf(savedEmployees))
                .create()
                .stream().peek(it -> it.setPeriodData(PeriodData.values()[monthOrdinal.incrementAndGet()])).toList();
        var savedSharingsPerPeriod = limitSharingPerPeriodRepository.saveAll(sharingsPerPeriod);

        var spendings = Instancio.ofList(LimitSpending.class)
                .ignore(Select.field(LimitSpending::getId))
                .generate(Select.field(LimitSpending::getLimitSharingPerPeriod), gen -> gen.oneOf(savedSharingsPerPeriod))
                .generate(Select.field(LimitSpending::getEmployee), gen -> gen.oneOf(savedEmployees))
                .create();
        var savedSpendings = spendingRepository.saveAll(spendings);

        var actualMap = this.service.getSpendingSharings(savedSpendings.parallelStream().map(LimitSpending::getId).toList());

        assertThat(actualMap).hasSameSizeAs(savedSpendings);
        assertThat(actualMap.keySet()).hasSameElementsAs(savedSpendings.parallelStream().map(LimitSpending::getId).toList());
        assertThat(actualMap.values()).hasSameElementsAs(ReflectionUtils.cast(savedSpendings.stream().map(LimitSpending::getLimitSharingPerPeriod).map(LimitSharingPerPeriod::getLimitSharing).map(LimitSharing::getId).toList()));
    }

    @Test
    @DisplayName("Получение лимитов не в статусе")
    void test_getByLimitAndStatusNot() {
        var employees = Instancio.ofList(Employee.class)
                .create();
        var savedEmployees = employeeRepository.saveAll(employees);
        var organizations = Instancio.ofList(Organization.class)
                .create();
        var savedOrganizations = organizationRepository.saveAll(organizations);

        final var service = dslContext.insertInto(SERVICES)
                .values(Instancio.create(String.class))
                .returning(SERVICES.ID)
                .fetchSingle();

        var depLimits = Instancio.ofList(DepLimit.class)
                .ignore(Select.field(DepLimit::getId))
                .ignore(Select.field(DepLimit::getDepartment))
                .ignore(Select.field(DepLimit::getParentDepartment))
                .generate(Select.field(DepLimit::getAuthor), gen -> gen.oneOf(savedEmployees))
                .generate(Select.field(DepLimit::getLimitOwner), gen -> gen.oneOf(savedEmployees))
                .generate(Select.field(DepLimit::getOrganization), gen -> gen.oneOf(savedOrganizations))
                .set(Select.field(DepLimit::getLimitServiceType), service.getId())
                .create();
        var savedDepLimits = depLimitRepository.saveAll(depLimits);

        var transportTypeOrdinal = new AtomicInteger();
        var sharings = Instancio.ofList(LimitSharing.class)
                .ignore(Select.field(LimitSharing::getId))
                .generate(Select.field(LimitSharing::getLimit), gen -> gen.oneOf(savedDepLimits))
                .generate(Select.field(LimitSharing::getAuthor), gen -> gen.oneOf(savedEmployees))
                .supply(Select.field(LimitSharing::getTransportType), () -> TransportTypeEnum.values()[transportTypeOrdinal.incrementAndGet()])
                .create();
        var savedSharings = limitSharingRepository.saveAll(sharings);

        var monthOrdinal = new AtomicInteger();
        var sharingsPerPeriod = Instancio.ofList(LimitSharingPerPeriod.class)
                .ignore(Select.field(LimitSharingPerPeriod::getId))
                .generate(Select.field(LimitSharingPerPeriod::getLimitSharing), gen -> gen.oneOf(savedSharings))
                .generate(Select.field(LimitSharingPerPeriod::getAuthor), gen -> gen.oneOf(savedEmployees))
                .create()
                .stream().peek(it -> it.setPeriodData(PeriodData.values()[monthOrdinal.incrementAndGet()])).toList();
        var savedSharingsPerPeriod = limitSharingPerPeriodRepository.saveAll(sharingsPerPeriod);

        var spendings = Instancio.ofList(LimitSpending.class)
                .ignore(Select.field(LimitSpending::getId))
                .generate(Select.field(LimitSpending::getLimitSharingPerPeriod), gen -> gen.oneOf(savedSharingsPerPeriod))
                .generate(Select.field(LimitSpending::getEmployee), gen -> gen.oneOf(savedEmployees))
                .create();
        var savedSpendings = spendingRepository.saveAll(spendings);

        var actualMap = this.service.getByLimitAndStatusNot(depLimits.parallelStream().map(DepLimit::getId).collect(Collectors.toUnmodifiableSet()), LimitSpendingStatus.CANCELED);

        assertThat(actualMap).hasSameSizeAs(savedSpendings.parallelStream().filter(it -> LimitSpendingStatus.CANCELED != it.getStatus()).collect(Collectors.toMap(it -> it.getLimitSharingPerPeriod().getLimitSharing().getLimit().getId(), List::of, (l, r) -> Stream.concat(l.stream(), r.stream()).toList())));
        assertThat(actualMap.keySet()).hasSameElementsAs(savedSpendings.parallelStream().filter(it -> LimitSpendingStatus.CANCELED != it.getStatus()).map(LimitSpending::getLimitSharingPerPeriod).map(LimitSharingPerPeriod::getLimitSharing).map(LimitSharing::getLimit).map(Limit::getId).toList());
        assertThat(actualMap.values().stream().flatMap(Collection::stream).map(LimitSpending::getId).toList()).hasSameElementsAs(savedSpendings.parallelStream().filter(it -> LimitSpendingStatus.CANCELED != it.getStatus()).map(LimitSpending::getId).toList());
    }

}