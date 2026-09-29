package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsService;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка нового контроллера статистики")
@ActiveProfiles("test")
class LimitsStatsControllerImplTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private DepLimitService depLimitService;

    @MockitoBean
    private LimitStatsService limitStatsService;

    @MockitoBean
    private EmployeeOrganizationFunction function;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authorizationManager);
    }

    @Test
    @DisplayName("Проверка статистики лимитов")
    void test_getLimitStats() throws Exception {
        var now = LocalDate.now(ZoneOffset.UTC);
        var organizationId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var depLimit = Instancio.of(DepLimit.class).create();
        var stats = Instancio.of(LimitStatsV2DTO.class)
                .set(Select.field(LimitStatsV2DTO::getPeriod), Instancio.create(Month.class))
                .create();

        when(function.apply(null)).thenReturn(organizationId);
        when(depLimitService.getByDepartmentAndYearAndLimitServiceType(any(), any(), any()))
                .thenReturn(depLimit);
        when(limitStatsService.getLimitStatsV2(eq(organizationId), anyList(), any())).thenReturn(List.of(stats));

        mockMvc.perform(MockMvcRequestBuilders.get("/statistic/%s/department/%s".formatted(organizationId, departmentId))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.departmentId").value(stats.getDepartmentId().toString()),
                        jsonPath("$.departmentName").value(stats.getDepartmentName()),
                        jsonPath("$.departmentLevel").value(stats.getDepartmentLevel()),
                        jsonPath("$.year").value(stats.getYear()),
                        jsonPath("$.period").value(stats.getPeriod().name()),
                        jsonPath("$.employeesNumber").value(stats.getEmployeesNumber()),
                        jsonPath("$.budgetYear").value(stats.getBudgetYear().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.budgetPeriod").value(stats.getBudgetPeriod().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.sumSpentYear").value(stats.getSumSpentYear().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.sumSpentPeriod").value(stats.getSumSpentPeriod().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.sumReservedYear").value(stats.getSumReservedYear().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.sumBalanceYear").value(stats.getSumBalanceYear().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.perEmployeeBudget").value(stats.getPerEmployeeBudget().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.perEmployeeSpent").value(stats.getPerEmployeeSpent().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.currentDateEconomy").value(stats.getCurrentDateEconomy().multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.percentUsedYear").value(stats.getPercentUsedYear()),
                        jsonPath("$.percentUsedPeriod").value(stats.getPercentUsedPeriod()),
                        jsonPath("$.limitSharingStatsDTOList.length()").value(stats.getLimitSharingStatsDTOList().size())
                );
    }

    @Test
    @DisplayName("Проверка статистики лимитов. В файл")
    void test_getLimitStats_file() throws Exception {
        var now = LocalDate.now(ZoneOffset.UTC);
        var organizationId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var results = new ArrayList<LimitStatsV2DTO>();
        var stats = Instancio.ofList(LimitLevelDTO.class).create();
        var bytes = Instancio.of(byte[].class).create();

        when(function.apply(null)).thenReturn(organizationId);
        when(limitStatsService.getLimitStatsV2(any(), anyList(), any())).thenAnswer(inv -> {
            var result = Instancio.create(LimitStatsV2DTO.class);
            results.add(result);
            return List.of(result);
        });
        when(limitStatsService.getLimitsList(eq(organizationId), any(), any(UUID.class))).thenReturn(stats);
        when(limitStatsService.exportToXlsxV2(results)).thenAnswer(inv -> {
            Files.createDirectories(Path.of("target/test/file"));
            var path = Path.of("target/test/file/file.data");
            Files.write(path, bytes);
            return path;
        });

        mockMvc.perform(MockMvcRequestBuilders.get("/statistic/%s/department/%s".formatted(organizationId, departmentId))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpectAll(
                        status().isOk(),
                        content().bytes(bytes)
                );
    }

    @Test
    @DisplayName("Проверка статистики лимитов. Лимит не найден")
    void test_getLimitStats_notFound() throws Exception {
        var defaultUntilDate = LocalDate.now(ZoneOffset.UTC).minusMonths(1);
        var organizationId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var stats = Instancio.of(LimitStatsV2DTO.class)
                .set(Select.field(LimitStatsV2DTO::getPeriod), Instancio.create(Month.class))
                .create();

        when(function.apply(null)).thenReturn(organizationId);
        when(limitStatsService.getLimitStatsV2(eq(organizationId), anyList(), eq(defaultUntilDate))).thenReturn(List.of(stats));

        mockMvc.perform(MockMvcRequestBuilders.get("/statistic/%s/department/%s".formatted(organizationId, departmentId))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.entity.id.departmentId").value(departmentId.toString()),
                        jsonPath("$.entity.id.year").value(defaultUntilDate.getYear()),
                        jsonPath("$.entity.name").value(Limit.class.getSimpleName())
                );
    }

}