package issues;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.LimitsApplication;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.database.limits.Tables.SERVICES;
import static ru.sber.transport.database.limits.Tables.TYPES;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@SpringBootTest(classes = LimitsApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles({"test", "import"})
@DisplayName("Проверка импорта")
public class ImportExportIssueTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LimitSharingPerPeriodRepository limitSharingPerPeriodRepository;

    @Autowired
    private LimitSharingRepository limitSharingRepository;

    @Autowired
    private LimitRepository<?> limitRepository;

    @MockitoBean
    private AuthorizationManager<?> roleManager;

    @Autowired
    private DSLContext dslContext;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(roleManager);

        dslContext.insertInto(SERVICES)
                .values("PASSENGER")
                .execute();

        dslContext.insertInto(TYPES)
                .values("TAXI")
                .execute();
    }

    @AfterEach
    void tearDown() {
        limitSharingPerPeriodRepository.deleteAll();
        limitSharingRepository.deleteAll();
        limitRepository.deleteAll();
        organizationRepository.deleteAll();
        departmentRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("Проверка импорта")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_import() throws Exception {
        var organization = organizationRepository.saveAndFlush(Instancio.create(Organization.class));

        var department1 = Instancio.of(Department.class)
                .set(Select.field(Department::getCode), "1111")
                .set(Select.field(Department::getDepartmentName), "MYDEP1")
                .set(Select.field(Department::getOrganizationId), organization.getId())
                .set(Select.field(Department::isActive), true)
                .ignore(Select.field(Department::getParentId))
                .create();
        department1 = departmentRepository.saveAndFlush(department1);

        var department2 = Instancio.of(Department.class)
                .set(Select.field(Department::getCode), "2222")
                .set(Select.field(Department::getDepartmentName), "MYDEP1")
                .set(Select.field(Department::getOrganizationId), organization.getId())
                .set(Select.field(Department::getParentId), department1.getId())
                .set(Select.field(Department::isActive), true)
                .create();

        department2 = departmentRepository.saveAndFlush(department2);

        var entity = Instancio.of(Employee.class)
                .set(Select.field(Employee::getPersonnelNumber), "91111")
                .set(Select.field(Employee::getFirstName), "IVAN")
                .set(Select.field(Employee::getLastName), "IVANOV")
                .set(Select.field(Employee::getDepartmentId), department2.getId())
                .set(Select.field(Employee::getOrganizationId), organization.getId())
                .set(Select.field(Employee::isActive), true)
                .create();
        var employee = employeeRepository.saveAndFlush(entity);

        var file = new MockMultipartFile("file", "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n", Files.readAllBytes(Path.of("src/test/resources/excel/import_limits.xlsx")));

        mockMvc.perform(multipart("/limits/import/%s".formatted(organization.getId()))
                        .file(file)
                .with(jwt().jwt(builder -> builder.jti(employee.getUserId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("SUCCESS"));
    }

}
