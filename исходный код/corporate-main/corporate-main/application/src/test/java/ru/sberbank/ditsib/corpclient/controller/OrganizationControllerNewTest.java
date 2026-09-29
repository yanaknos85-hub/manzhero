package ru.sberbank.ditsib.corpclient.controller;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.CheckUserAccessService;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.model.Position;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
@DisplayName("Проверка контроллера организаций  (новый)")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@ActiveProfiles("test")
class OrganizationControllerNewTest {

    private static final String USER1_ID = "00000000-0000-0000-0000-000000000000";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private OrganizationRepository organizationRepository;

    @MockitoSpyBean
    private CheckUserAccessService checkAccessService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    public static final String IMAGES = "target/test/images/logo";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void clear() {
        var root = new File(IMAGES);

        if (root.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(root.listFiles())).forEach(File::delete);

            root.delete();
        }
    }

    @DisplayName("Проверка фильтра")
    @Test
    void test_filtered() throws Exception {
        for (var i = 0; i < 20; i++) {
            var organization = new Organization();
            organization.setAddress("Add%sress%s".formatted(i / 10, i));
            organization.setOfficialName("Official%sName%s".formatted(i / 10, i));
            organization.setTid("T%sid%s".formatted(i / 10, i));
            organization.setMsrn("Ms%srn%s".formatted(i / 10, i));
            organization.setOrganizationCode(i);

            organizationRepository.save(organization);
        }

        doNothing().when(checkAccessService).check();

        var check = mockMvc.perform(get("/?officialName=Official1")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value("Official1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value("Add1ress1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value("T1id1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value("Ms1rn1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(i + 10));

        }

        check = mockMvc.perform(get("/?address=Add1")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value("Official1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value("Add1ress1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value("T1id1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value("Ms1rn1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(i + 10));

        }

        check = mockMvc.perform(get("/?tid=T1")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value("Official1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value("Add1ress1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value("T1id1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value("Ms1rn1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(i + 10));
        }

        check = mockMvc.perform(get("/?msrn=Ms1")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value("Official1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value("Add1ress1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value("T1id1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value("Ms1rn1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(i + 10));
        }
    }

    @DisplayName("Проверка сортировки")
    @Test
    void test_sorted() throws Exception {
        doNothing().when(checkAccessService).check();

        var orgs = new ArrayList<Organization>();

        for (var i = 0; i < 20; i++) {
            var organization = new Organization();
            organization.setAddress("Address%02d".formatted(i));
            organization.setOfficialName("OfficialName%02d".formatted(20 - i));
            organization.setTid("Tid%02d".formatted(i));
            organization.setMsrn("Msrn%02d".formatted(20 - i));
            organization.setOrganizationCode(20 - i);

            orgs.add(organizationRepository.save(organization));
        }

        var check = mockMvc.perform(get("/?field=address")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()));
        }

        check = mockMvc.perform(get("/?field=address&direction=DESC")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()));
        }

        check = mockMvc.perform(get("/?field=officialName")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()));
        }

        check = mockMvc.perform(get("/?field=officialName&direction=DESC")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()));
        }

        check = mockMvc.perform(get("/?field=tid")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()));
        }

        check = mockMvc.perform(get("/?field=tid&direction=DESC")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()));
        }

        check = mockMvc.perform(get("/?field=msrn")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()));
        }

        check = mockMvc.perform(get("/?field=msrn&direction=DESC")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()));
        }
    }

    @DisplayName("Проверка пагинации")
    @Test
    void test_paginated() throws Exception {
        doNothing().when(checkAccessService).check();

        var orgs = new ArrayList<Organization>();

        for (var i = 0; i < 100; i++) {
            var organization = new Organization();
            organization.setAddress("Address%03d".formatted(i));
            organization.setOfficialName("OfficialName%03d".formatted(i));
            organization.setTid("Tid%03d".formatted(i));
            organization.setMsrn("Msrn%03d".formatted(i));
            organization.setOrganizationCode(i);

            orgs.add(organizationRepository.save(organization));
        }

        var check = mockMvc.perform(get("/?page=1&size=30")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(30))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.size").value(30))
                .andExpect(jsonPath("$.number").value(1));

        for (var i = 0; i < 30; i++) {
            var checkOrg = orgs.stream().skip(30).toList().get(i);

            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()))
                    .andExpect(jsonPath("$.content[%s].status".formatted(i)).value(checkOrg.getStatus().name()))
            ;
        }

        check = mockMvc.perform(get("/?page=0&size=30")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(30))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.size").value(30))
                .andExpect(jsonPath("$.number").value(0))
        ;

        for (var i = 0; i < 30; i++) {
            var checkOrg = new ArrayList<>(orgs).get(i);

            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()))
                    .andExpect(jsonPath("$.content[%s].status".formatted(i)).value(checkOrg.getStatus().name()))
            ;
        }

        check = mockMvc.perform(get("/?page=2&size=30")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(30))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.size").value(30))
                .andExpect(jsonPath("$.number").value(2))
        ;

        for (var i = 0; i < 30; i++) {
            var checkOrg = orgs.stream().skip(60).toList().get(i);

            check
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(checkOrg.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].officialName".formatted(i)).value(checkOrg.getOfficialName()))
                    .andExpect(jsonPath("$.content[%s].address".formatted(i)).value(checkOrg.getAddress()))
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(checkOrg.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].tid".formatted(i)).value(checkOrg.getTid()))
                    .andExpect(jsonPath("$.content[%s].organizationCode".formatted(i)).value(checkOrg.getOrganizationCode()))
                    .andExpect(jsonPath("$.content[%s].status".formatted(i)).value(checkOrg.getStatus().name()))
            ;
        }
    }

    @SuppressWarnings("java:S5961")
    @DisplayName("Проверка получения списка сотрудников по списку организаций и департаментов")
    @Test
    void test_getCustomers() throws Exception {
        doNothing().when(checkAccessService).check(any(UUID.class));

        var orgs = new ArrayList<Organization>();
        for (var i = 0; i < 10; i++) {
            var organization = new Organization();
            organization.setAddress("address%02d".formatted(10 - i));
            organization.setOfficialName("name%02d".formatted(i * i));
            organization.setMsrn("msrn");
            organization.setTid("tid");
            orgs.add(organizationRepository.save(organization));
        }

        var deps = new ArrayList<Department>();
        for (var i = 0; i < 20; i++) {
            var department = new Department();
            department.setName("Department%02d".formatted(20 - i));
            department.setCode("Code%02d".formatted(20 - i));
            department.setOrganization(orgs.get(i < 10 ? i : 19 - i));
            department.setHumanReadableId("HRD%02d".formatted(i * i));
            department.setUpdateTime(OffsetDateTime.now());
            deps.add(departmentRepository.save(department));
        }

        var poss = new ArrayList<Position>();
        for (var i = 0; i < 20; i++) {
            var position = new Position();
            position.setName("Position%02d".formatted(20 - i));
            position.setOrganization(orgs.get(i < 10 ? i : 19 - i));
            position.setHumanReadableId("HRP%02d".formatted(i * i));
            poss.add(positionRepository.save(position));
        }

        var emps = new ArrayList<Employee>();

        for (var i = 0; i < 20; i++) {
            var department = deps.get(i < 10 ? i : 19 - i);
            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setDepartment(department);
            employee.setLastName("LastName%02d".formatted(i));
            employee.setFirstName("FirstName%02d".formatted(i));
            employee.setPersonnelNumber("PersonnelNumber%02d".formatted(100 - i));
            employee.setPatronymic("Patronymic%02d".formatted(i));
            employee.setHumanReadableId("HumanReadable%02d".formatted(i));
            employee.setMobilePhone("Mobile%02d".formatted(i));
            employee.setEmail("Email%02d".formatted(i));
            employee.setPosition(poss.get(i < 10 ? i : 19 - i));
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());

            emps.add(employeeRepository.save(employee));
        }

        var check = mockMvc.perform(get("/employees/search_eg?organizations=" + orgs.getFirst().getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(emps.getFirst().getId()).toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        String responseString = check.andReturn().getResponse().getContentAsString();
        assertTrue(responseString.contains(emps.getFirst().getFirstName()));
        assertTrue(responseString.contains(emps.getFirst().getLastName()));
        assertTrue(responseString.contains(emps.getFirst().getPersonnelNumber()));
        assertTrue(responseString.contains(emps.get(19).getFirstName()));
        assertTrue(responseString.contains(emps.get(19).getLastName()));
        assertTrue(responseString.contains(emps.get(19).getPersonnelNumber()));
        assertFalse(responseString.contains(emps.get(1).getFirstName()));
        assertFalse(responseString.contains(emps.get(2).getLastName()));
        assertFalse(responseString.contains(emps.get(18).getPersonnelNumber()));

        mockMvc.perform(get("/employees/search_eg?organizations=" + orgs.getFirst().getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(emps.get(5).getId()).toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/employees/search_eg?organizations=" + orgs.getFirst().getId().toString() + "," + orgs.get(1).getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(emps.getFirst().getId()).toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().is4xxClientError());

        check = mockMvc.perform(get("/employees/search_eg?organizations=" + orgs.getFirst().getId().toString() + "," + orgs.get(5).getId().toString())
                        .with(jwt().jwt(builder -> builder.claim("data_master", true).jti(Objects.requireNonNull(emps.getFirst().getId()).toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        responseString = check.andReturn().getResponse().getContentAsString();
        assertTrue(responseString.contains(emps.getFirst().getFirstName()));
        assertTrue(responseString.contains(emps.getFirst().getLastName()));
        assertTrue(responseString.contains(emps.getFirst().getPersonnelNumber()));
        assertTrue(responseString.contains(emps.get(5).getFirstName()));
        assertTrue(responseString.contains(emps.get(5).getLastName()));
        assertTrue(responseString.contains(emps.get(5).getPersonnelNumber()));
        assertTrue(responseString.contains(emps.get(19).getFirstName()));
        assertTrue(responseString.contains(emps.get(19).getLastName()));
        assertTrue(responseString.contains(emps.get(19).getPersonnelNumber()));
        assertTrue(responseString.contains(emps.get(14).getFirstName()));
        assertTrue(responseString.contains(emps.get(14).getLastName()));
        assertTrue(responseString.contains(emps.get(14).getPersonnelNumber()));
        assertFalse(responseString.contains(emps.get(1).getFirstName()));
        assertFalse(responseString.contains(emps.get(2).getLastName()));
        assertFalse(responseString.contains(emps.get(18).getPersonnelNumber()));

        //Проверка обязательности queryParam
        mockMvc.perform(get("/employees/search_eg?departments=" + deps.getFirst().getId().toString() + "," + deps.get(1).getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(emps.getFirst().getId()).toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/employees/search_eg?organizations=[]")
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(emps.getFirst().getId()).toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/employees/search_eg?organizations={}")
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(emps.getFirst().getId()).toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/employees/search_eg?organizations=")
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(emps.getFirst().getId()).toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().is4xxClientError());
    }
}
