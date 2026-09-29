package issues;

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
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.LimitsApplication;
import ru.sberbank.ditsib.transport.limits.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.database.limits.Tables.SERVICES;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@SpringBootTest(classes = LimitsApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles({"test", "import"})
@DisplayName("Проверка проблемы TRANSPORT-29026")
public class Transport29026Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepLimitRepository depLimitRepository;

    @Autowired
    private DSLContext dslContext;

    @BeforeEach
    void setUp() {
        AuthorizeUtils.authorize(authorizationManager);
    }

    @Test
    @DisplayName("Фильтр по инициалам")
    void test_getFiltered() throws Exception {
        var organization = organizationRepository.save(Instancio.create(Organization.class));
        var employee = employeeRepository.save(Instancio.of(Employee.class)
                        .set(Select.field(Employee::getOrganizationId), organization.getId())
                .create());

        final var service = dslContext.insertInto(SERVICES)
                .values(Instancio.create(String.class))
                .returning(SERVICES.ID)
                .fetchSingle();

        var depLimit = depLimitRepository.save(Instancio.of(DepLimit.class)
                        .ignore(Select.field(DepLimit::getParent))
                        .ignore(Select.field(DepLimit::getParentDepartment))
                        .ignore(Select.field(DepLimit::getDepartment))
                        .ignore(Select.field(DepLimit::getSharings))
                        .ignore(Select.field(DepLimit::getId))
                        .set(Select.field(DepLimit::getOrganization), organization)
                        .set(Select.field(DepLimit::getLimitServiceType), service.getId())
                        .set(Select.field(DepLimit::getLimitOwner), employee)
                        .set(Select.field(DepLimit::getAuthor), employee)
                .create());

        var content = """
            {
                "year":%s,
                "humanReadableLimitId":"",
                "limitOwner":"%s %s.",
                "limitType":null,
                "parentDepartment":""
            }
            """.formatted(depLimit.getYear(), employee.getLastName(), employee.getFirstName().charAt(0));

        mockMvc.perform(post("/limits/searchPageable/%s?page=0&size=20".formatted(organization.getId()))
                        .with(jwt().jwt(it -> it.jti(employee.getUserId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(depLimit.getId().toString()));
    }

    @Test
    @DisplayName("Фильтр по табелю")
    void test_getFiltered_personnel() throws Exception {
        var organization = organizationRepository.save(Instancio.create(Organization.class));
        var employee = employeeRepository.save(Instancio.of(Employee.class)
                        .set(Select.field(Employee::getOrganizationId), organization.getId())
                .create());

        final var service = dslContext.insertInto(SERVICES)
                .values(Instancio.create(String.class))
                .returning(SERVICES.ID)
                .fetchSingle();

        var depLimit = depLimitRepository.save(Instancio.of(DepLimit.class)
                        .ignore(Select.field(DepLimit::getParent))
                        .ignore(Select.field(DepLimit::getParentDepartment))
                        .ignore(Select.field(DepLimit::getDepartment))
                        .ignore(Select.field(DepLimit::getSharings))
                        .ignore(Select.field(DepLimit::getId))
                        .set(Select.field(DepLimit::getOrganization), organization)
                        .set(Select.field(DepLimit::getLimitServiceType), service.getId())
                        .set(Select.field(DepLimit::getLimitOwner), employee)
                        .set(Select.field(DepLimit::getAuthor), employee)
                .create());

        var content = """
            {
                "year":%s,
                "humanReadableLimitId":"",
                "limitOwner":"%s",
                "limitType":null,
                "parentDepartment":""
            }
            """.formatted(depLimit.getYear(), employee.getPersonnelNumber());

        mockMvc.perform(post("/limits/searchPageable/%s?page=0&size=20".formatted(organization.getId()))
                        .with(jwt().jwt(it -> it.jti(employee.getUserId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(depLimit.getId().toString()));
    }

}
