package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPercents;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка нового контроллера лимитов подразделений")
@ActiveProfiles("test")
class DepLimitControllerImplTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private DepLimitService depLimitService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private DepartmentRepository departmentRepository;

    @MockitoBean
    private LimitSharingPercentService limitSharingPercentService;

    @MockitoBean
    private EmployeeOrganizationFunction employeeOrganizationFunction;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> authManager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authManager);
    }

    @Test
    @DisplayName("Добавление")
    void test_add() throws Exception {
        var organizationId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(Select.field(Organization::getId), organizationId)
                .create();
        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganizationId), organizationId)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), userId)
                .set(Select.field(Employee::getUserId), userId)
                .set(Select.field(Employee::getOrganizationId), organizationId)
                .set(Select.field(Employee::getDepartmentId), department.getId())
                .create();
        var head = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), department.getDepartmentHead().getId())
                .set(Select.field(Employee::getUserId), department.getDepartmentHead().getId())
                .set(Select.field(Employee::getOrganizationId), organizationId)
                .create();
        var depLimit = Instancio.of(DepLimit.class)
                .create();
        var year = Instancio.create(Integer.class);
        var limitServiceType = Instancio.create(String.class);

        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(employee.getDepartmentId())).thenReturn(Optional.of(department));
        when(employeeService.getByUserId(userId)).thenReturn(Optional.of(employee));
        when(employeeService.get(department.getDepartmentHead().getId())).thenReturn(Optional.of(head));
        when(organizationService.get(organizationId)).thenReturn(Optional.of(organization));
        when(departmentService.getUpperLevelDepartment(organizationId)).thenReturn(List.of(department));
        when(depLimitService.getByDepartmentAndYearAndLimitServiceType(department.getId(), year, limitServiceType)).thenReturn(null);
        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);
        when(depLimitService.add(any(DepLimit.class))).thenAnswer(inv -> {
            var limit = inv.getArgument(0, DepLimit.class);
            limit.setId(UUID.randomUUID());
            limit.setHumanReadableId(UUID.randomUUID().toString());
            return limit;
        });

        mockMvc.perform(post("/organization/%s/department".formatted(organizationId))
                        .with(jwt().jwt(builder -> builder.jti(userId.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "year": %s,
                                "limitSharingType": "%s",
                                "limitServiceType": "%s",
                                "sum": %s,
                                "parentId": %s,
                                "finalSharing": %s,
                                "useThisLimit": %s
                                }
                                """.formatted(year, LimitSharingType.PERCENTS, "PASSENGER", 1000, null, true, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.humanReadableId").isNotEmpty())
                .andExpect(jsonPath("$.limitOwner").value(head.getId().toString()))
                .andExpect(jsonPath("$.limitStatus").value(LimitStatus.PLANNING.name()))
                .andExpect(jsonPath("$.year").value(year))
                .andExpect(jsonPath("$.limitSharingType").value(LimitSharingType.PERCENTS.name()))
                .andExpect(jsonPath("$.limitServiceType").value("PASSENGER"))
                .andExpect(jsonPath("$.sum").value(1000))
                .andExpect(jsonPath("$.reserve").value(1000))
                .andExpect(jsonPath("$.economy").value(0))
                .andExpect(jsonPath("$.finalSharing").value("true"))
                .andExpect(jsonPath("$.useThisLimit").value("true"))
                .andExpect(jsonPath("$.limitType").value(LimitType.DEPARTMENT.name()))
                .andExpect(jsonPath("$.department.id").value(department.getId().toString()))
                .andExpect(jsonPath("$.department.code").value(department.getCode()))
                .andExpect(jsonPath("$.department.humanReadableId").value(department.getHumanReadableId()))
                .andExpect(jsonPath("$.employee").isEmpty())
                .andExpect(jsonPath("$.parentLimitId").isEmpty())
                .andExpect(jsonPath("$.sharings").isEmpty())
                .andExpect(jsonPath("$.owner.id").value(head.getId().toString()))
        ;

        var depLimitCaptor = ArgumentCaptor.forClass(DepLimit.class);
        var limitSharingPercentsCaptor = ArgumentCaptor.forClass(LimitSharingPercents.class);

        verify(depLimitService).add(depLimitCaptor.capture());
        verify(limitSharingPercentService).add(limitSharingPercentsCaptor.capture());

        assertPojos(depLimitCaptor.getValue(), depLimit, "department", "reserve", "economy");
        var actual = limitSharingPercentsCaptor.getValue();
        assertThat(actual.getJanuary()).isEqualTo(8);
        assertThat(actual.getFebruary()).isEqualTo(8);
        assertThat(actual.getMarch()).isEqualTo(8);
        assertThat(actual.getApril()).isEqualTo(8);
        assertThat(actual.getMay()).isEqualTo(8);
        assertThat(actual.getJune()).isEqualTo(8);
        assertThat(actual.getJuly()).isEqualTo(8);
        assertThat(actual.getAugust()).isEqualTo(8);
        assertThat(actual.getSeptember()).isEqualTo(8);
        assertThat(actual.getOctober()).isEqualTo(8);
        assertThat(actual.getNovember()).isEqualTo(8);
        assertThat(actual.getDecember()).isEqualTo(12);
        assertPojos(actual.getLimit(), depLimit, "department", "reserve", "economy");
        assertPojos(actual.getAuthor(), employee);
    }

    @Test
    @DisplayName("Добавление. Конфликт")
    void test_add_conflict() throws Exception {
        var organizationId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(Select.field(Organization::getId), organizationId)
                .create();
        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getOrganizationId), organizationId)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), userId)
                .set(Select.field(Employee::getUserId), userId)
                .set(Select.field(Employee::getOrganizationId), organizationId)
                .set(Select.field(Employee::getDepartmentId), department.getId())
                .create();
        var head = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), department.getDepartmentHead().getId())
                .set(Select.field(Employee::getUserId), department.getDepartmentHead().getId())
                .set(Select.field(Employee::getOrganizationId), organizationId)
                .create();
        var depLimit = Instancio.of(DepLimit.class)
                .create();
        var year = Instancio.create(Integer.class);
        var limitServiceType = "PASSENGER";

        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee));
        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);
        when(departmentRepository.findById(employee.getDepartmentId())).thenReturn(Optional.of(department));
        when(employeeService.getByUserId(userId)).thenReturn(Optional.of(employee));
        when(employeeService.get(department.getDepartmentHead().getId())).thenReturn(Optional.of(head));
        when(organizationService.get(organizationId)).thenReturn(Optional.of(organization));
        when(departmentService.getUpperLevelDepartment(organizationId)).thenReturn(List.of(department));
        when(depLimitService.getByDepartmentAndYearAndLimitServiceType(department.getId(), year, limitServiceType)).thenReturn(depLimit);

        mockMvc.perform(post("/organization/%s/department".formatted(organizationId))
                        .with(jwt().jwt(builder -> builder.jti(userId.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "year": %s,
                                "limitSharingType": "%s",
                                "limitServiceType": "%s",
                                "sum": %s,
                                "parentId": %s,
                                "finalSharing": %s,
                                "useThisLimit": %s
                                }
                                """.formatted(year, LimitSharingType.PERCENTS, "PASSENGER", 1000, null, true, true)))
                .andExpect(status().isConflict())
        ;

        verify(depLimitService, never()).add(any(DepLimit.class));
        verify(limitSharingPercentService, never()).add(any());
    }

    private <T> void assertPojos(T actual, T expected, String... exclusions) throws IllegalAccessException {
        var excludes = List.of(exclusions);
        for (var field : actual.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!excludes.contains(field.getName())) {
                assertThat(field.get(actual)).describedAs("Checking " + field.getName()).isEqualTo(field.get(expected));
            }
        }
    }

}