package issues;

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
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.CorporateClientApplication;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.database.corporate.Tables.ORGANIZATION;

@SpringBootTest(classes = CorporateClientApplication.class)
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@EmbeddedKafka
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Проверка проблемы TRANSPORT-29061")
class Transport29061Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DSLContext context;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private AuthorizationManager<?> authManager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authManager);
    }

    @Test
    @DisplayName("Проверка получения организации")
    void test_get_organization() throws Exception {
        var saved = organizationRepository.saveAndFlush(Instancio.of(Organization.class)
                .ignore(Select.field(Organization::getId))
                .ignore(Select.field(Organization::getContacts))
                .ignore(Select.field(Organization::getPositions))
                .ignore(Select.field(Organization::getDepartments))
                .ignore(Select.field(Organization::getEmployees))
                .ignore(Select.field(Organization::getOrganizationGroup))
                .set(Select.field(Organization::getStatus), OrganizationStatus.ACTIVE)
                .create());

        var position = positionRepository.save(Instancio.of(Position.class)
                .ignore(Select.field(Position::getId))
                .ignore(Select.field(Position::getEmployees))
                .set(Select.field(Position::getOrganization), saved)
                .create());

        var department = departmentRepository.save(Instancio.of(Department.class)
                .ignore(Select.field(Department::getId))
                .ignore(Select.field(Department::getEmployees))
                .ignore(Select.field(Department::getHead))
                .set(Select.field(Department::getOrganization), saved)
                .create());

        var employee = employeeRepository.save(Instancio.of(Employee.class)
                .ignore(Select.field(Employee::getAttributes))
                .ignore(Select.field(Employee::getSupervisor))
                .ignore(Select.field(Employee::getSupervisorOf))
                .ignore(Select.field(Employee::getDelegatedBy))
                .ignore(Select.field(Employee::getDelegateRecords))
                .ignore(Select.field(Employee::getPersonalCars))
                .ignore(Select.field(Employee::getAvailableTransportTypes))
                .set(Select.field(Employee::getOrganization), saved)
                .set(Select.field(Employee::getPosition), position)
                .set(Select.field(Employee::getDepartment), department)
                .create());

        mockMvc.perform(get("/%s".formatted(saved.getId()))
                        .with(jwt().jwt(it -> it.jti(employee.getUserId().toString()).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.officialName").value(saved.getOfficialName()))
                .andExpect(jsonPath("$.address").value(saved.getAddress()))
                .andExpect(jsonPath("$.easupId").value(saved.getSyncId()))
        ;

        mockMvc.perform(get("/").with(jwt().jwt(it -> it.jti(employee.getUserId().toString()).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/")
                        .with(jwt().jwt(it -> it.jti(employee.getUserId().toString()).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.content[0].officialName").value(saved.getOfficialName()))
                .andExpect(jsonPath("$.content[0].address").value(saved.getAddress()))
                .andExpect(jsonPath("$.content[0].easupId").value(saved.getSyncId()))
        ;
    }

    @Test
    @DisplayName("Проверка добавления организации")
    void test_post_organization() throws Exception {
        var data = """
                {
                    "officialName": "Name",
                    "address": "Address",
                    "tid": "Tid",
                    "msrn": "Msrn",
                    "easupId": "EasupId"
                }
                """;

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(organizationRepository.findAll().getFirst().getId().toString()))
                .andExpect(jsonPath("$.officialName").value("Name"))
                .andExpect(jsonPath("$.officialName").value(organizationRepository.findAll().getFirst().getOfficialName()))
                .andExpect(jsonPath("$.address").value("Address"))
                .andExpect(jsonPath("$.address").value(organizationRepository.findAll().getFirst().getAddress()))
                .andExpect(jsonPath("$.easupId").value("EasupId"))
                .andExpect(jsonPath("$.easupId").value(context.select(ORGANIZATION.SYNC_ID).from(ORGANIZATION).where(ORGANIZATION.ID.eq(organizationRepository.findAll().getFirst().getId())).fetchSingle(0)))
                .andExpect(jsonPath("$.tid").value("Tid"))
                .andExpect(jsonPath("$.tid").value(organizationRepository.findAll().getFirst().getTid()))
                .andExpect(jsonPath("$.msrn").value("Msrn"))
                .andExpect(jsonPath("$.msrn").value(organizationRepository.findAll().getFirst().getMsrn()))
        ;
    }

}
