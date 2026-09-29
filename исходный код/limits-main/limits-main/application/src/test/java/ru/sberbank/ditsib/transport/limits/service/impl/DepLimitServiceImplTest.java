package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.config.HibernateEventsConfiguration;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@Transactional
@SpringBootTest
@AutoConfigureDataJpa
@Import(DepLimitServiceImpl.class)
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка репозитория УЗ")
@ActiveProfiles("test")
@MockitoBean(types = {
        EmployeesGrpc.EmployeesBlockingStub.class,
        DepartmentsGrpc.DepartmentsBlockingStub.class
})
class DepLimitServiceImplTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private LimitService limitService;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private EmpLimitService empLimitService;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private LimitSharingService limitSharingService;

    @MockitoBean
    private LimitHistoryService limitHistoryService;

    @MockitoBean
    private HibernateEventsConfiguration hibernateEventsConfiguration;

    @MockitoBean(name = "sQGeneratorLimits")
    private SQGenerator sqGenerator;

    @MockitoBean
    private Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices;

    @MockitoBean
    private LimitSharingPercentService limitSharingPercentService;

    @MockitoBean
    private LimitSpendingService limitSpendingService;

    @MockitoBean
    private ObjectProvider<DepLimitServiceImpl> depLimitServicesProvider;

    @MockitoSpyBean
    private DepLimitRepository depLimitRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepLimitService depLimitService;

    @Test
    @DisplayName("Проверка добавления")
    void test_add_new() {
        var organization = organizationRepository.save(Instancio.create(Organization.class));
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(Select.field(Department::getOrganizationId), organization.getId())
                .create());

        var author = employeeRepository.save(Instancio.of(Employee.class)
                .set(Select.field(Employee::getDepartmentId), department.getId())
                .set(Select.field(Employee::getOrganizationId), organization.getId())
                .create());

        var depLimit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getOrganization), organization)
                .set(Select.field(DepLimit::getDepartment), department)
                .set(Select.field(DepLimit::getAuthor), author)
                .ignore(Select.field(DepLimit::getLimitOwner))
                .ignore(Select.field(DepLimit::getId))
                .ignore(Select.field(DepLimit::getParent))
                .ignore(Select.field(DepLimit::getParentDepartment))
                .ignore(Select.field(DepLimit::getSharings))
                .create();

        when(sqGenerator.getNextId(any(), any())).thenReturn("HRI");

        var saved = depLimitService.add(depLimit);

        assertThat(saved.getDepartment().getId()).isEqualTo(department.getId());
        assertThat(saved.getReserve()).isEqualTo(depLimit.getReserve());
        assertThat(saved.getEconomy()).isEqualTo(depLimit.getEconomy());
        assertThat(saved.getLimitType()).isEqualTo(depLimit.getLimitType());
        assertThat(saved.getHumanReadableId()).isEqualTo(depLimit.getHumanReadableId());
        assertThat(saved.getAuthor().getId()).isEqualTo(depLimit.getAuthor().getId());
        assertThat(saved.getLimitStatus()).isEqualTo(depLimit.getLimitStatus());
        assertThat(saved.getYear()).isEqualTo(depLimit.getYear());
        assertThat(saved.getLimitSharingType()).isEqualTo(depLimit.getLimitSharingType());
        assertThat(saved.getLimitServiceType()).isEqualTo(depLimit.getLimitServiceType());
        assertThat(saved.getSum()).isEqualTo(depLimit.getSum());
        assertThat(saved.isFinalSharing()).isEqualTo(depLimit.isFinalSharing());
        assertThat(saved.isUseThisLimit()).isEqualTo(depLimit.isUseThisLimit());
        assertThat(saved.getCreationTime()).isEqualTo(depLimit.getCreationTime());
        assertThat(saved.getOrganization().getId()).isEqualTo(depLimit.getOrganization().getId());
    }

    @Test
    @DisplayName("Проверка получения лимитов. Нет данных")
    void test_getByDepartment_noData() {
        var departmentId = UUID.randomUUID();

        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getId), departmentId)
                .ignore(Select.field(Department::getParentId))
                .create();

        when(departmentService.get(departmentId)).thenReturn(Optional.of(department));

        assertThat(depLimitService.getByDepartment(departmentId)).isEmpty();
    }

    @Test
    @DisplayName("Проверка получения лимитов. Есть данные текущего подразделения")
    void test_getByDepartment_currentDepartment() {
        var departmentId = UUID.randomUUID();

        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getId), departmentId)
                .create();
        var depLimit = Instancio.ofList(DepLimit.class)
                .create();

        when(departmentService.get(departmentId)).thenReturn(Optional.of(department));
        when(depLimitRepository.findByDepartmentId(departmentId)).thenReturn(depLimit);

        var actualList = depLimitService.getByDepartment(departmentId);

        assertThat(actualList).isNotEmpty();

        for (var i = 0; i < actualList.size(); i++) {
            int finalI = i;
            assertSoftly(it -> {
                it.assertThat(actualList.get(finalI).getId()).isEqualTo(depLimit.get(finalI).getId());
                it.assertThat(actualList.get(finalI).getEconomy()).isEqualTo(depLimit.get(finalI).getEconomy());
                it.assertThat(actualList.get(finalI).getReserve()).isEqualTo(depLimit.get(finalI).getReserve());
                it.assertThat(actualList.get(finalI).getYear()).isEqualTo(depLimit.get(finalI).getYear());
                it.assertThat(actualList.get(finalI).getSum()).isEqualTo(depLimit.get(finalI).getSum());
                it.assertThat(actualList.get(finalI).getCreationTime()).isEqualTo(depLimit.get(finalI).getCreationTime());
                it.assertThat(actualList.get(finalI).getHumanReadableId()).isEqualTo(depLimit.get(finalI).getHumanReadableId());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения лимитов. Есть данные подразделения выше")
    void test_getByDepartment_upperDepartment() {
        var departmentId = UUID.randomUUID();

        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getId), departmentId)
                .create();
        var parent = Instancio.of(Department.class)
                .set(Select.field(Department::getId), department.getParentId())
                .create();
        var depLimit = Instancio.ofList(DepLimit.class)
                .create();

        when(departmentService.get(departmentId)).thenReturn(Optional.of(department));
        when(departmentService.get(department.getParentId())).thenReturn(Optional.of(parent));
        when(depLimitRepository.findByDepartmentIdAndYearAndLimitStatus(department.getParentId(), OffsetDateTime.now().getYear(), LimitStatus.SHARED)).thenReturn(depLimit);

        var actualList = depLimitService.findAccessible(departmentId);

        assertThat(actualList).isNotEmpty();

        for (var i = 0; i < actualList.size(); i++) {
            int finalI = i;
            assertSoftly(it -> {
                it.assertThat(actualList.get(finalI).getId()).isEqualTo(depLimit.get(finalI).getId());
                it.assertThat(actualList.get(finalI).getEconomy()).isEqualTo(depLimit.get(finalI).getEconomy());
                it.assertThat(actualList.get(finalI).getReserve()).isEqualTo(depLimit.get(finalI).getReserve());
                it.assertThat(actualList.get(finalI).getYear()).isEqualTo(depLimit.get(finalI).getYear());
                it.assertThat(actualList.get(finalI).getSum()).isEqualTo(depLimit.get(finalI).getSum());
                it.assertThat(actualList.get(finalI).getCreationTime()).isEqualTo(depLimit.get(finalI).getCreationTime());
                it.assertThat(actualList.get(finalI).getHumanReadableId()).isEqualTo(depLimit.get(finalI).getHumanReadableId());
            });
        }
    }
}