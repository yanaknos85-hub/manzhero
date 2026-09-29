package ru.sberbank.ditsib.corpclient.controller;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.file_works.database.migrations.DatabaseMigration;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.database.model.messages.Role;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка импорта/экспорта сотрудников")
@ActiveProfiles({"test", "import"})
@Import(DatabaseMigration.class)
class EmployeeImportExportTest {
    
    public static final String USER_ID = "647f3aba-4367-4d0e-93aa-bd537ba694ee";

    @MockitoBean
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private RolesRepository rolesRepository;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private UploadStates uploadStates;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    private final int rowsCount = 100;
    
    @BeforeEach
    void setup() {
        for (var i = 1; i <= rowsCount; i++) {
            var organization = new Organization();
            organization.setOfficialName("Организация " + i);
            organization.setAddress("Адрес " + i);
            organization.setDigitId((long) i);
            organization.setMsrn("msrn " + i);
            organization.setTid("tid " + i);

            organization = organizationRepository.save(organization);
            
            var department = new Department();
            department.setName("Подразделение " + i);
            department.setCode("Code " + i);
            department.setOrganization(organization);
            department.setHumanReadableId("HRI " + i);
            department.setUpdateTime(OffsetDateTime.now());

            department = departmentRepository.save(department);
            
            var position = new Position();
            position.setName("Должность " + i);
            position.setActiveStatus(ActiveStatus.ACTIVE);
            position.setHumanReadableId("HRID " + i);
            position.setOrganization(organization);
            
            position = positionRepository.save(position);
            
            var supervisor = new Employee();
            supervisor.setId(UUID.randomUUID());
            supervisor.setNew(true);
            supervisor.setPersonnelNumber((100 + i) + "");
            supervisor.setLastName("LN " + i);
            supervisor.setFirstName("FN " + i);
            supervisor.setDepartment(department);
            supervisor.setPosition(position);
            supervisor.setOrganization(department.getOrganization());
            supervisor.setHumanReadableId("HRISE " + i);
            supervisor.setUpdateTime(OffsetDateTime.now());

            employeeRepository.save(supervisor);
            
            var role = new Role();
            role.setCode("Code" + i);
            role.setName("Роль " + i);
            rolesRepository.save(role);
        }
    }
    
    @Test
    @DisplayName("Импорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void import_data() throws Exception {
        var file = new MockMultipartFile("file", "employee.xlsx",
                                         "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                         getClass().getClassLoader().getResourceAsStream("load/employees.xlsx"));
        
        var employeesCount = rowsCount;
        
        mockMvc.perform(multipart("/files/employee/").file(file)
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk());
    
        await().until(() -> uploadStates.getResults("employee", USER_ID).size(), equalTo(1));
        await().timeout(Duration.ofSeconds(30)).until(() -> uploadStates.getResults("employee", USER_ID).getFirst().getFinished(), equalTo(true));
        
        assertThat(uploadStates.getResults("employee", USER_ID).getFirst().getPages().stream().allMatch(page -> page.getExceptionStrings().isEmpty()))
                   .isTrue();
        assertThat(uploadStates.getResults("employee", USER_ID).getFirst().getPages().stream().map(PageInfo::getRow)
                               .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty()))
                   .isTrue();
        Specification<Employee> spec = (root, query, builder) -> root.get(Employee_.supervisor).get(Employee_.personnelNumber).isNotNull();
        var actualSlaves = employeeRepository.findAll(spec, Sort.by(Employee_.HUMAN_READABLE_ID));
        
        assertThat(actualSlaves).hasSize(employeesCount);
        
        for (var i = 0; i < employeesCount; i++) {
            var number = i + 1;
            var actual = actualSlaves.get(i);

            var department = departmentRepository.findById(actual.getDepartment().getId()).orElseThrow();
            var organization =
                    organizationRepository.findById(department.getOrganization().getId()).orElseThrow();
            var position = positionRepository.findById(actual.getPosition().getId()).orElseThrow();
            
            assertAll(() -> {
                assertThat(actual.getPersonnelNumber()).isEqualTo(String.format("%03d", number));
                assertThat(actual.getLastName()).isEqualTo("Фамилия" + number);
                assertThat(actual.getFirstName()).isEqualTo("Имя" + number);
                assertThat(actual.getPatronymic()).isEqualTo("Отчество" + number);
                assertThat(actual.getEmail()).isEqualTo("some@email.com " + number);
                assertThat(actual.getMobilePhone()).isEqualTo("" + (79000000000L + number));
                assertThat(department.getName()).isEqualTo("Подразделение " + number);
                assertThat(organization.getOfficialName())
                        .isEqualTo("Организация " + number);
                assertThat(position.getName()).isEqualTo("Должность " + number);
                assertThat(actual.getSupervisor().getPersonnelNumber()).isEqualTo("" + (100 + number));
            });
        }
    }
    
}
