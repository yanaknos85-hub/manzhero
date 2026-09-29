package ru.sberbank.ditsib.corpclient.controller;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.RoleActionType;
import ru.sberbank.ditsib.corpclient.database.model.RoleHistory;
import ru.sberbank.ditsib.corpclient.shared.SharedData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка контроллера добавления/удаления роли курьера")
@Transactional
class EmployeeCourierRoleControllerTest extends SharedData {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private RoleHistoryRepository roleHistoryRepository;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @BeforeEach
    void init() {
        AuthorizeUtils.authorize(authorizationManager);
        roleHistoryRepository.deleteAll();

        organizationRepository.saveAndFlush(testOrganization1);
        testOrganization1 = organizationRepository.findById(testOrganization1.getId()).orElseThrow();

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee1.setNew(false);
        testEmployee1 = employeeRepository.save(testEmployee1);

    }

    @Test
    @DisplayName("Проверка добавления роли курьера")
    void test_addCourierRole_success() throws Exception {
        mockMvc.perform(
                        put("/self/courier")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();

        List<RoleHistory> historyList = roleHistoryRepository.findByEmployeeId(testEmployee1.getId());
        assertThat(historyList).hasSize(1);
        assertThat(historyList.get(0).getActionType()).isEqualTo(RoleActionType.ADD_COURIER);
        assertThat(historyList.get(0).getRoleNames()).isEqualTo("[ROLE_USER, ROLE_COURIER]");
    }

    @Test
    @DisplayName("Проверка повторного добавления роли курьера")
    void test_addCourierRole_repeat() throws Exception {
        long initialHistoryCount = roleHistoryRepository.count();

        mockMvc.perform(
                        put("/self/courier")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"),
                                                new SimpleGrantedAuthority("ROLE_COURIER")))
                                .contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        assertThat(roleHistoryRepository.count()).isEqualTo(initialHistoryCount);
    }

    @Test
    @DisplayName("Проверка удаления роли курьера")
    void test_deleteCourierRole_success() throws Exception {

        mockMvc.perform(
                        delete("/self/courier")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"),
                                                new SimpleGrantedAuthority("ROLE_COURIER")))
                                .contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();

        List<RoleHistory> historyList = roleHistoryRepository.findByEmployeeId(testEmployee1.getId());
        assertThat(historyList).hasSize(1);
        assertThat(historyList.get(0).getActionType()).isEqualTo(RoleActionType.REMOVE_COURIER);
        assertThat(historyList.get(0).getRoleNames()).isEqualTo("[ROLE_USER]");
    }

    @Test
    @DisplayName("Проверка повторного удаления роли курьера")
    void test_deleteCourierRole_repeat() throws Exception {
        long initialHistoryCount = roleHistoryRepository.count();

        mockMvc.perform(
                        delete("/self/courier")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();
        assertThat(roleHistoryRepository.count()).isEqualTo(initialHistoryCount);
    }
}
