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
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
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
@DisplayName("Проверка контроллера сотрудников (новый)")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class EmployeeControllerNewTest {
    
    private static final String USER1_ID = "00000000-0000-0000-0000-000000000000";

    @MockitoBean
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @MockitoSpyBean
    private CheckUserAccessService checkAccessService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    public static final String IMAGES = "target/test/images/logo";
    
    @AfterEach
    public void clear() {
        var root = new File(IMAGES);
    
        if (root.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            Arrays.stream(Objects.requireNonNull(root.listFiles())).forEach(File::delete);

            //noinspection ResultOfMethodCallIgnored
            root.delete();
        }
    }
    
    @DisplayName("Проверка фильтра")
    @Test
    void test_filtered() throws Exception {
        doNothing().when(checkAccessService).check(any(UUID.class));
        
        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization = organizationRepository.save(organization);
        
        var department = new Department();
        department.setName("Department");
        department.setCode("Code");
        department.setOrganization(organization);
        department.setHumanReadableId("HRD");
        department.setUpdateTime(OffsetDateTime.now());
        department = departmentRepository.save(department);
        
        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        position = positionRepository.save(position);
        
        for (var i = 0; i < 20; i++) {
            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setDepartment(department);
            employee.setLastName("Last%sName%02d".formatted(i / 10, i));
            employee.setFirstName("First%sName%02d".formatted(i / 10, i));
            employee.setPersonnelNumber("Personnel%sNumber%02d".formatted(i / 10, i));
            employee.setPatronymic("Patrony%smic%02d".formatted(i / 10, i));
            employee.setHumanReadableId("Human%sReadable%02d".formatted(i / 10, i));
            employee.setMobilePhone("Mob%sile%02d".formatted(i / 10, i));
            employee.setEmail("Em%sail%02d".formatted(i / 10, i));
            employee.setPosition(position);
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());

            employeeRepository.save(employee);
        }
        
        var check = mockMvc.perform(get("/%s/departments/%s/employees?fullName=Last1".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value("Last1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value("First1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value("Mob1ile1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value("Em1ail1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value("Personnel1Number1%s".formatted(i)))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?fullName=First1".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value("Last1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value("First1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value("Mob1ile1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value("Em1ail1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value("Personnel1Number1%s".formatted(i)))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?fullName=Patrony1".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value("Last1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value("First1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value("Mob1ile1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value("Em1ail1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value("Personnel1Number1%s".formatted(i)))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?personnelNumber=Personnel1".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value("Last1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value("First1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value("Mob1ile1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value("Em1ail1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value("Personnel1Number1%s".formatted(i)))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?mobilePhone=Mob1".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value("Last1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value("First1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value("Mob1ile1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value("Em1ail1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value("Personnel1Number1%s".formatted(i)))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?email=Em1".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value("Last1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value("First1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value("Mob1ile1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value("Em1ail1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value("Personnel1Number1%s".formatted(i)))
            ;
        }
    }
    
    @DisplayName("Проверка сортировки")
    @Test
    void test_sorted() throws Exception {
        doNothing().when(checkAccessService).check(any(UUID.class));
    
        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization = organizationRepository.save(organization);
    
        var department = new Department();
        department.setName("Department");
        department.setCode("Code");
        department.setOrganization(organization);
        department.setHumanReadableId("HRD");
        department.setUpdateTime(OffsetDateTime.now());
        department = departmentRepository.save(department);
    
        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        position = positionRepository.save(position);
        
        var orgs = new ArrayList<Employee>();
    
        for (var i = 0; i < 20; i++) {
            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setDepartment(department);
            employee.setLastName("LastName%02d".formatted(i));
            employee.setFirstName("FirstName%02d".formatted(i));
            employee.setPersonnelNumber("PersonnelNumber%02d".formatted(20 - i));
            employee.setPatronymic("Patronymic%02d".formatted(i));
            employee.setHumanReadableId("HumanReadable%02d".formatted(i));
            employee.setMobilePhone("Mobile%02d".formatted(i));
            employee.setEmail("Email%02d".formatted(i));
            employee.setPosition(position);
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());
        
            orgs.add(employeeRepository.save(employee));
        }
        
        var check = mockMvc.perform(get("/%s/departments/%s/employees/?field=fullName".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?field=fullName&direction=DESC".formatted(organization.getId(),
                                                                                                            department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?field=personnelNumber".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?field=personnelNumber&direction=DESC".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?field=humanReadableId".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?field=humanReadableId&direction=DESC".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
    }
    
    @DisplayName("Проверка пагинации")
    @Test
    void test_paginated() throws Exception {
    
        doNothing().when(checkAccessService).check(any(UUID.class));
    
        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization = organizationRepository.save(organization);
    
        var department = new Department();
        department.setName("Department");
        department.setCode("Code");
        department.setOrganization(organization);
        department.setHumanReadableId("HRD");
        department.setUpdateTime(OffsetDateTime.now());
        department = departmentRepository.save(department);
    
        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        position = positionRepository.save(position);
    
        var orgs = new ArrayList<Employee>();
    
        for (var i = 0; i < 100; i++) {
            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setDepartment(department);
            employee.setLastName("LastName%02d".formatted(i));
            employee.setFirstName("FirstName%02d".formatted(i));
            employee.setPersonnelNumber("PersonnelNumber%02d".formatted(20 - i));
            employee.setPatronymic("Patronymic%02d".formatted(i));
            employee.setHumanReadableId("HumanReadable%02d".formatted(i));
            employee.setMobilePhone("Mobile%02d".formatted(i));
            employee.setEmail("Email%02d".formatted(i));
            employee.setPosition(position);
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());
        
            orgs.add(employeeRepository.save(employee));
        }
        
        var check = mockMvc.perform(get("/%s/departments/%s/employees/?page=1&size=30".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(30))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.size").value(30))
                .andExpect(jsonPath("$.number").value(1))
        ;
        
        for (var i = 0; i < 30; i++) {
            var checkOrg = orgs.stream().skip(30).toList().get(i);
            
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?page=2&size=30".formatted(organization.getId(), department.getId()))
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
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/%s/employees/?page=0&size=30".formatted(organization.getId(), department.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(30))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.size").value(30))
                .andExpect(jsonPath("$.number").value(0))
        ;
        
        for (var i = 0; i < 30; i++) {
            var checkOrg = orgs.stream().skip(0).toList().get(i);
            
            check
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(checkOrg.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(checkOrg.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].mobilePhone".formatted(i)).value(checkOrg.getMobilePhone()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(checkOrg.getEmail()))
                    .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(i)).value(checkOrg.getPersonnelNumber()))
            ;
        }
    }
}
