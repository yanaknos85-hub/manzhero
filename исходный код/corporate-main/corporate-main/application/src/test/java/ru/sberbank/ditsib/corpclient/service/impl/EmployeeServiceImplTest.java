package ru.sberbank.ditsib.corpclient.service.impl;

import io.qameta.allure.Feature;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientMetricAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.grpc.EasupEmployeeGrpcClient;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.corpclient.mapper.EmployeeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.*;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.PositionService;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Проверка сервиса сотрудников")
@Transactional
@ActiveProfiles("test")
class EmployeeServiceImplTest {

    private static final String ORGANIZATION_EASUP_ID = "EASUP_ORG_0001";
    private static final String DEPARTMENT_EASUP_ID = "EASUP_DEP_OOO1";
    private static final String POSITION_EASUP_ID = "EASUP_POS_0001";

    @MockitoBean
    private EasupEmployeeGrpcClient easupEmployeeGrpcClient;

    @MockitoBean
    private GrpcClientMetricAutoConfiguration autoConfiguration;

    @MockitoBean
    private EmployeeSender employeeSender;

    @MockitoBean
    private UserSender userSender;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DepartmentService departmentService;

    @MockitoBean
    private DepartmentSender departmentSender;

    @Autowired
    private PositionService positionService;

    @Autowired
    private EmployeeService employeeService;

    @MockitoBean
    private PositionSender positionSender;

    @MockitoBean
    private OrganizationSender organizationSender;

    private final SQGenerator sqGenerator = mock(SQGenerator.class);

    private Organization organization;

    private Department department;

    private Position position;

    @BeforeEach
    void beforeEach() {
        when(sqGenerator.getNextId(any(Prefix.class), anyLong())).then(inv -> "%s-%04d-%08d".formatted(inv.getArgument(0), inv.getArgument(1, Long.class), new Random().nextInt(99999999)));

        organization = new Organization();
        organization.setAddress("Address");
        organization.setOfficialName("Name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization.setSyncId(ORGANIZATION_EASUP_ID);

        organization = organizationRepository.save(organization);

        department = new Department();
        department.setOrganization(organization);
        department.setCode("Code");
        department.setName("Name");
        department.setHumanReadableId("HRIDep");
        department.setUpdateTime(OffsetDateTime.now());

        department = departmentRepository.save(department);

        position = new Position();
        position.setName("Name");
        position.setOrganization(organization);
        position.setHumanReadableId("HRIPos");

        position = positionRepository.save(position);
    }

    @Test
    @DisplayName("Добавление")
    void test_add() {
        var expected = Employee.builder()
                .department(this.department)
                .id(UUID.randomUUID())
                .organization(this.organization)
                .position(this.position)
                .lastName("Last name")
                .firstName("First name")
                .humanReadableId("HRIEmp")
                .build();

        var actual = employeeService.saveEmployee(expected, null);

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getDepartment().getId()).isEqualTo(expected.getDepartment().getId());
        assertThat(actual.getOrganization().getId()).isEqualTo(expected.getOrganization().getId());
        assertThat(actual.getPosition().getId()).isEqualTo(expected.getPosition().getId());
        assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
        assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
        assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
    }

    @Test
    @DisplayName("Добавление. Подразделение в другой организации")
    void test_add_otherOrganizationDepartment() {
        var organization = new Organization();
        organization.setAddress("Address2");
        organization.setOfficialName("Name2");

        organization = organizationRepository.save(organization);

        var position = new Position();
        position.setName("Name2");
        position.setOrganization(organization);
        position.setHumanReadableId("HRIPos2");

        position = positionRepository.save(position);

        var expected = Employee.builder()
                .department(this.department)
                .id(UUID.randomUUID())
                .organization(organization)
                .position(position)
                .lastName("Last name")
                .firstName("First name")
                .humanReadableId("HRIEmp")
                .build();

        try {
            employeeService.saveEmployee(expected, null);
            employeeRepository.flush();
            fail("Have to throw an exception");
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Test
    @DisplayName("Добавление. Должность в другой организации")
    void test_add_otherOrganizationPosition() {
        var organization = new Organization();
        organization.setAddress("Address2");
        organization.setOfficialName("Name2");

        organization = organizationRepository.save(organization);

        var department = new Department();
        department.setOrganization(organization);
        department.setCode("Code2");
        department.setName("Name2");
        department.setHumanReadableId("HRIDep2");

        department = departmentRepository.save(department);

        var expected = Employee.builder()
                .department(department)
                .id(UUID.randomUUID())
                .organization(organization)
                .position(this.position)
                .lastName("Last name")
                .firstName("First name")
                .humanReadableId("HRIEmp")
                .build();

        try {
            employeeService.saveEmployee(expected, null);
            employeeRepository.flush();
            fail("Have to throw an exception");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

}