package ru.sberbank.ditsib.corpclient.controller;

import io.qameta.allure.Feature;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.CheckUserAccessService;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Organization;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера подразделений (расширенный поиск)")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@ActiveProfiles("test")
class OrganizationDepartmentControllerSearchTest {
    private static final String USER1_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @MockitoSpyBean
    private CheckUserAccessService checkAccessService;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    private Department save;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @DisplayName("Проверка получения подразделений по фильтрам")
    @Test
    void test_filtered() throws Exception {
        doNothing().when(checkAccessService).check(any(UUID.class));

        var organization1 = createOrganization(1);
        var organization2 = createOrganization(2);

        for (var i = 0; i < 30; i++) {
            int smallIndex = i / 10;
            var department = new Department();
            department.setOrganization(i%2 == 0?organization1:organization2);
            department.setName("Department%sName%s".formatted(smallIndex, i));
            department.setCode("Co%sde%s".formatted(smallIndex, i));
            department.setHumanReadableId("Human%sReadable%s".formatted(smallIndex, i));
            department.setLocation("Loca%stion%s".formatted(smallIndex, i));
            department.setUpdateTime(OffsetDateTime.now());
            department.setActiveStatus(i < 20 ? ActiveStatus.ACTIVE  : ActiveStatus.INACTIVE);
            departmentRepository.save(department);
        }

        doNothing().when(checkAccessService).check();

        var check = mockMvc.perform(get("/departments?projection=MIN&status=ACTIVE&organizations=%s&organizations=%s&departmentName=department1".formatted(organization1.getId(), organization2.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.[%s].organizationId".formatted(i))
                            .value(i%2==0 ? organization1.getId().toString() : organization2.getId().toString()))
                    .andExpect(jsonPath("$.[%s].departmentName".formatted(i)).value("Department1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.[%s].code".formatted(i)).value("Co1de1%s".formatted(i)))
                    .andExpect(jsonPath("$.[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.[%s].location".formatted(i)).value("Loca1tion1%s".formatted(i)));
        }

        check = mockMvc.perform(get("/departments?projection=MIN&status=ACTIVE&organizations=%s&organizations=%s&departmentName=department1".formatted(organization1.getId(), UUID.randomUUID()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
        int step =  0;
        for (var i = 0; i < 5; i++) {
            check
                    .andExpect(jsonPath("$.[%s].organizationId".formatted(i)).value(organization1.getId().toString()))
                    .andExpect(jsonPath("$.[%s].departmentName".formatted(i)).value("Department1Name1%s".formatted(step)))
                    .andExpect(jsonPath("$.[%s].code".formatted(i)).value("Co1de1%s".formatted(step)))
                    .andExpect(jsonPath("$.[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(step)))
                    .andExpect(jsonPath("$.[%s].location".formatted(i)).value("Loca1tion1%s".formatted(step)));
            step += 2;
        }

        mockMvc.perform(get("/departments?projection=MIN&organizations=%s".formatted(organization1.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(15));

        mockMvc.perform(get("/departments?projection=MIN&organizations=%s&&status=INACTIVE".formatted(organization1.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        mockMvc.perform(get("/departments?projection=MIN&organizations=%s&&status=ACTIVE&size=5".formatted(organization1.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        mockMvc.perform(get("/departments?projection=MIN&organizations=%s&organizations=%s&size=100".formatted(organization1.getId(), organization2.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(30));

        mockMvc.perform(get("/departments?projection=MIN&size=100")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/departments?projection=SELECT&organizations=%s&organizations=%s&size=100".formatted(organization1.getId(), organization2.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(30));

        mockMvc.perform(get("/departments?projection=FULL&organizations=%s&organizations=%s&size=100".formatted(organization1.getId(), organization2.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(30));

        mockMvc.perform(get("/departments?organizations=%s&organizations=%s&size=100".formatted(organization1.getId(), organization2.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(30));

        mockMvc.perform(get("/departments?organizations=%s&organizations=%s&size=100&status=SOMETHING".formatted(organization1.getId(), organization2.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(30));

        List<UUID> departmentIds = departmentRepository.findAll().stream()
                .map(Department::getId)
                .limit(3).toList();

        mockMvc.perform(get("/departments?departments=%s&departments=%s&departments=%s".formatted(departmentIds.toArray()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

    }

    @NotNull
    private Organization createOrganization(int number) {
        var organization1 = new Organization();
        organization1.setAddress("address" + number);
        organization1.setOfficialName("name" + number);
        organization1.setMsrn("msrn");
        organization1.setTid("tid");
        organization1 = organizationRepository.save(organization1);
        return organization1;
    }

}
