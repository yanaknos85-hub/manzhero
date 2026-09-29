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
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
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
@DisplayName("Проверка контроллера подразделений (новый)")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@ActiveProfiles("test")
class OrganizationDepartmentControllerNewTest {
    
    private static final String USER1_ID = "00000000-0000-0000-0000-000000000000";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @MockitoSpyBean
    private CheckUserAccessService checkAccessService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

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
        doNothing().when(checkAccessService).check(any(UUID.class));
        
        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("msrn");
        organization.setTid("tid");

        organization = organizationRepository.save(organization);
        
        for (var i = 0; i < 20; i++) {
            var department = new Department();
            department.setOrganization(organization);
            department.setName("Department%sName%s".formatted(i / 10, i));
            department.setCode("Co%sde%s".formatted(i / 10, i));
            department.setHumanReadableId("Human%sReadable%s".formatted(i / 10, i));
            department.setLocation("Loca%stion%s".formatted(i / 10, i));
            department.setUpdateTime(OffsetDateTime.now());

            departmentRepository.save(department);
        }
        
        doNothing().when(checkAccessService).check();
        
        var check = mockMvc.perform(get("/%s/departments/?departmentName=Department1".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value("Department1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value("Co1de1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value("Loca1tion1%s".formatted(i)));
        }
        
        check = mockMvc.perform(get("/%s/departments/?code=Co1".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value("Department1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value("Co1de1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value("Loca1tion1%s".formatted(i)));
        }
        
        check = mockMvc.perform(get("/%s/departments/?humanReadableId=Human1".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value("Department1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value("Co1de1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value("Loca1tion1%s".formatted(i)));
        }
        
        check = mockMvc.perform(get("/%s/departments/?location=Loca1".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
        for (var i = 0; i < 10; i++) {
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value("Department1Name1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value("Co1de1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value("Human1Readable1%s".formatted(i)))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value("Loca1tion1%s".formatted(i)));
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
        
        var orgs = new ArrayList<Department>();
    
        for (var i = 0; i < 20; i++) {
            var department = new Department();
            department.setOrganization(organization);
            department.setName("DepartmentName%03d".formatted(i));
            department.setCode("Code%03d".formatted(20 - i));
            department.setHumanReadableId("HumanReadable%03d".formatted(i));
            department.setLocation("Location%03d".formatted(i));
            department.setUpdateTime(OffsetDateTime.now());
        
            orgs.add(departmentRepository.save(department));
        }
        
        var check = mockMvc.perform(get("/%s/departments/?field=departmentName".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/?field=departmentName&direction=DESC".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()))
            ;
        }
        
        check = mockMvc.perform(get("/%s/departments/?field=code".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()));
        }
        
        check = mockMvc.perform(get("/%s/departments/?field=code&direction=DESC".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()));
        }
        
        check = mockMvc.perform(get("/%s/departments/?field=location".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(i);
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()));
        }
        
        check = mockMvc.perform(get("/%s/departments/?field=location&direction=DESC".formatted(organization.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk())
                           .andExpect(jsonPath("$.content.length()").value(20));
        
        for (var i = 0; i < orgs.size(); i++) {
            var checkOrg = orgs.get(orgs.size() - (i + 1));
            check
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()));
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
    
        var orgs = new ArrayList<Department>();
    
        for (var i = 0; i < 100; i++) {
            var department = new Department();
            department.setOrganization(organization);
            department.setName("DepartmentName%03d".formatted(i));
            department.setCode("Code%03d".formatted(20 - i));
            department.setHumanReadableId("HumanReadable%03d".formatted(i));
            department.setLocation("Location%03d".formatted(i));
            department.setUpdateTime(OffsetDateTime.now());
        
            orgs.add(departmentRepository.save(department));
        }
        
        var check = mockMvc.perform(get("/%s/departments/?page=1&size=30".formatted(organization.getId()))
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
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()));
        }
        
        check = mockMvc.perform(get("/%s/departments/?page=2&size=30".formatted(organization.getId()))
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
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()));
        }
        
        check = mockMvc.perform(get("/%s/departments/?page=0&size=30".formatted(organization.getId()))
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
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(checkOrg.getName()))
                    .andExpect(jsonPath("$.content[%s].code".formatted(i)).value(checkOrg.getCode()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(checkOrg.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].location".formatted(i)).value(checkOrg.getLocation()));
        }
    }
}
