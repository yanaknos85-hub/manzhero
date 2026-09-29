package ru.sberbank.ditsib.transport.limits.controller.impl;

import io.qameta.allure.Feature;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.Month;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка работы импорта-экспорта")
@ActiveProfiles({"test", "import"})
class ImportExportControllerImplTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @MockitoBean
    private EmployeeOrganizationFunction employeeOrganizationFunction;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @MockitoBean
    private LimitStatsService limitStatsService;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager);
    }

    @Test
    @DisplayName("Экспорт")
    void test_export() throws Exception {
        var userId = UUID.randomUUID();
        var organization = organizationRepository.save(Instancio.create(Organization.class));
        var department = departmentRepository.save(Instancio.of(Department.class).set(Select.field(Department::getOrganizationId), organization.getId()).create());
        employeeRepository.save(Instancio.of(Employee.class).set(Select.field(Employee::getDepartmentId), department.getId()).set(Select.field(Employee::getUserId), userId).create());
        var year = Instancio.create(Integer.class);

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organization.getId());

        mockMvc.perform(MockMvcRequestBuilders.get("/limits/exportAsync/org/%s/year/%s".formatted(organization.getId(), year))
                .with(jwt().jwt(builder -> builder.jti(userId.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.started").value(true),
                jsonPath("$.result_url").value(new BaseMatcher<String>() {
                    @Override
                    public boolean matches(Object o) {
                        return ((String) o).startsWith("limits-") && ((String) o).endsWith(".XLSX");
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                })
            );
    }

    @Test
    @DisplayName("Экспорт статистики")
    void test_exportStats() throws Exception {
        var organizationId = UUID.randomUUID();
        var year = Instancio.create(Integer.class);
        var month = Instancio.create(Month.class).ordinal() + 1;
        var day = new Random().nextInt(1, 28);
        var userId = UUID.randomUUID();
        var limits = Instancio.createList(LimitLevelDTO.class);
        var stats = Instancio.createList(LimitStatsDTO.class);
        var path = Path.of("target/test/files");
        var filePath = Path.of(path.toString(), "file.bin");

        Files.createDirectories(path);
        Files.writeString(filePath, "data");

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);
        when(limitStatsService.getLimitsList(organizationId, year, null)).thenReturn(limits);
        when(limitStatsService.getLimitStats(eq(organizationId), anyList(), any())).thenReturn(stats);
        when(limitStatsService.exportToXlsx(stats)).thenReturn(filePath);

        mockMvc.perform(MockMvcRequestBuilders.get("/limits/exportstats/%s/%s/%s/%s".formatted(organizationId, year, month, day))
                .with(jwt().jwt(builder -> builder.jti(userId.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpectAll(
                status().isOk(),
                header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Report_%04d-%02d-%02d.xlsx\"".formatted(year, month, day)),
                content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                content().bytes("data".getBytes(StandardCharsets.UTF_8))
            );

        Files.delete(filePath);
    }

}