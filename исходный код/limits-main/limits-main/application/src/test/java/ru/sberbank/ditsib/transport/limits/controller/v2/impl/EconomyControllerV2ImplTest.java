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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dto.v2.EconomyV2DTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.limits.constants.LimitSharingType.MONTHLY;
import static ru.sberbank.ditsib.transport.limits.model.limit.Month.*;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent"})
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка экономии")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EconomyControllerV2ImplTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @MockitoBean
    private DepLimitService depLimitService;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private LimitTransferHistoryService limitTransferHistoryService;

    @MockitoBean
    private LimitSharingService limitSharingService;

    @MockitoSpyBean
    private LimitSharingPerPeriodService<Month> limitSharingPerPeriodService;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager);
    }

    @Test
    @DisplayName("Получение рассчетной экономии за год. Пусто")
    void test_getTransfersToEconomy_empty() throws Exception {
        var limitId = UUID.randomUUID();
        var year = Instancio.create(Integer.class);
        var depLimit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getLimitSharingType), MONTHLY)
                .create();

        when(depLimitService.get(limitId)).thenReturn(Optional.of(depLimit));

        mockMvc.perform(MockMvcRequestBuilders.get("/economy/%s/future?year=%s".formatted(limitId, year))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty())
        ;
    }

    @Test
    @DisplayName("Получение рассчетной экономии за год. Нет целевого лимита")
    void test_getTransfersToEconomy_noTargetLimit() throws Exception {
        var limitId = UUID.randomUUID();
        var year = Instancio.create(Integer.class);
        var depLimit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getLimitSharingType), MONTHLY)
                .create();
        var organizationId = UUID.randomUUID();

        when(depLimitService.get(limitId)).thenReturn(Optional.of(depLimit));
        for (var month : Month.values()) {
            var history = Instancio.ofList(LimitTransferHistory.class)
                    .set(Select.field(LimitTransferHistory::getSourcePeriod), PeriodData.valueOf(month.name()))
                    .set(Select.field(LimitTransferHistory::getTargetPeriod), PeriodData.valueOf(month.name()))
                    .create();
            when(limitTransferHistoryService.getHistoryTransfersToEconomy(organizationId, "PASSENGER", year, month)).thenReturn(history);
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/economy/%s/future?year=%s".formatted(limitId, year))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty())
        ;
    }

    @Test
    @DisplayName("Получение рассчетной экономии за год. Нет подразделения")
    void test_getTransfersToEconomy_noDepartment() throws Exception {
        var limitId = UUID.randomUUID();
        var year = Instancio.create(Integer.class);
        var depLimit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getLimitSharingType), MONTHLY)
                .create();
        var organizationId = UUID.randomUUID();

        when(depLimitService.get(limitId)).thenReturn(Optional.of(depLimit));
        for (var month : Month.values()) {
            var targetLimitId = UUID.randomUUID();
            var history = Instancio.ofList(LimitTransferHistory.class)
                    .set(Select.field(LimitTransferHistory::getSourcePeriod), PeriodData.valueOf(month.name()))
                    .set(Select.field(LimitTransferHistory::getTargetPeriod), PeriodData.valueOf(month.name()))
                    .set(Select.field(LimitTransferHistory::getTargetLimitId), targetLimitId)
                    .create();
            when(depLimitService.get(targetLimitId)).thenReturn(Optional.of(depLimit));
            when(limitTransferHistoryService.getHistoryTransfersToEconomy(organizationId, "PASSENGER", year, month)).thenReturn(history);
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/economy/%s/future?year=%s".formatted(limitId, year))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Получение рассчетной экономии за год. Нет распределения")
    void test_getTransfersToEconomy_noSharing() throws Exception {
        var limitId = UUID.randomUUID();
        var year = Instancio.create(Integer.class);
        var depLimit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getLimitSharingType), MONTHLY)
                .create();
        var organizationId = UUID.randomUUID();

        when(depLimitService.get(limitId)).thenReturn(Optional.of(depLimit));
        for (var month : Month.values()) {
            var targetLimitId = UUID.randomUUID();
            var history = Instancio.ofList(LimitTransferHistory.class)
                    .set(Select.field(LimitTransferHistory::getSourcePeriod), PeriodData.valueOf(month.name()))
                    .set(Select.field(LimitTransferHistory::getTargetPeriod), PeriodData.valueOf(month.name()))
                    .set(Select.field(LimitTransferHistory::getTargetLimitId), targetLimitId)
                    .create();
            when(departmentService.get(depLimit.getDepartment().getId())).thenReturn(Optional.of(depLimit.getDepartment()));
            when(depLimitService.get(targetLimitId)).thenReturn(Optional.of(depLimit));
            when(limitTransferHistoryService.getHistoryTransfersToEconomy(organizationId, "PASSENGER", year, month)).thenReturn(history);
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/economy/%s/future?year=%s".formatted(limitId, year))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Получение рассчетной экономии за год")
    void test_getTransfersToEconomy() throws Exception {
        var limitId = UUID.randomUUID();
        var year = Instancio.create(Integer.class);
        var organizationId = UUID.randomUUID();
        var limitServiceType = "PASSENGER";
        var depLimit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getOrganization), Organization.builder().id(organizationId).build())
                .set(Select.field(DepLimit::getLimitServiceType), limitServiceType)
                .set(Select.field(DepLimit::getLimitSharingType), MONTHLY)
                .create();

        var limitSharing = Instancio.create(LimitSharing.class);
        when(departmentService.get(depLimit.getDepartment().getId())).thenReturn(Optional.of(depLimit.getDepartment()));
        when(limitSharingService.getByLimitAndTransportType(eq(depLimit), any(TransportTypeEnum.class))).thenReturn(limitSharing);
        when(depLimitService.get(limitId)).thenReturn(Optional.of(depLimit));
        var expectedMap = new HashMap<Month, EconomyV2DTO>();
        var limitSharings = new HashMap<Month, LimitSharingPerPeriod>();
        for (var month : Month.values()) {
            var targetLimitId = UUID.randomUUID();
            var history = Instancio.ofList(LimitTransferHistory.class)
                    .set(Select.field(LimitTransferHistory::getSourcePeriod), PeriodData.valueOf(month.name()))
                    .set(Select.field(LimitTransferHistory::getTargetPeriod), PeriodData.valueOf(month.name()))
                    .set(Select.field(LimitTransferHistory::getTargetLimitId), targetLimitId)
                    .set(Select.field(LimitTransferHistory::getTargetTransportType), limitSharing.getTransportType())
                    .create();
            var limitSharingPerPeriod = Instancio.of(LimitSharingPerPeriod.class)
                    .set(Select.field(LimitSharingPerPeriod::getPeriodData), PeriodData.valueOf(month.name()))
                    .create();
            limitSharings.put(month, limitSharingPerPeriod);
            when(depLimitService.get(targetLimitId)).thenReturn(Optional.of(depLimit));
            when(limitTransferHistoryService.getHistoryTransfersToEconomy(organizationId, limitServiceType, year, month)).thenReturn(history);
            var total = history.stream().map(LimitTransferHistory::getSum).reduce(BigDecimal::add).get();
            expectedMap.put(month, new EconomyV2DTO(
                    depLimit.getDepartment().getDepartmentName(),
                    ru.sberbank.ditsib.transport.limits.dto.v2.Month.valueOf(month.name()),
                    total,
                    limitSharingPerPeriod.getSum(),
                    limitSharing.getTransportType(),
                    total.multiply(BigDecimal.valueOf(100)).divide(limitSharingPerPeriod.getSum(), MathContext.DECIMAL64).longValue()
            ));
        }
        when(limitSharingPerPeriodService.getByLimitSharing(any())).thenReturn(limitSharings);

        mockMvc.perform(MockMvcRequestBuilders.get("/economy/%s/future?year=%s".formatted(limitId, year))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].period").value("JANUARY"))
                .andExpect(jsonPath("$.[0].department").value(expectedMap.get(JANUARY).department()))
                .andExpect(jsonPath("$.[0].sumEconomy").value(expectedMap.get(JANUARY).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[0].sumPerPeriod").value(expectedMap.get(JANUARY).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[0].transportType").value(expectedMap.get(JANUARY).transportType().name()))
                .andExpect(jsonPath("$.[0].percent").value(expectedMap.get(JANUARY).percent()))
                .andExpect(jsonPath("$.[1].period").value("FEBRUARY"))
                .andExpect(jsonPath("$.[1].department").value(expectedMap.get(FEBRUARY).department()))
                .andExpect(jsonPath("$.[1].sumEconomy").value(expectedMap.get(FEBRUARY).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[1].sumPerPeriod").value(expectedMap.get(FEBRUARY).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[1].transportType").value(expectedMap.get(FEBRUARY).transportType().name()))
                .andExpect(jsonPath("$.[1].percent").value(expectedMap.get(FEBRUARY).percent()))
                .andExpect(jsonPath("$.[2].period").value("MARCH"))
                .andExpect(jsonPath("$.[2].department").value(expectedMap.get(MARCH).department()))
                .andExpect(jsonPath("$.[2].sumEconomy").value(expectedMap.get(MARCH).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[2].sumPerPeriod").value(expectedMap.get(MARCH).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[2].transportType").value(expectedMap.get(MARCH).transportType().name()))
                .andExpect(jsonPath("$.[2].percent").value(expectedMap.get(MARCH).percent()))
                .andExpect(jsonPath("$.[3].period").value("APRIL"))
                .andExpect(jsonPath("$.[3].department").value(expectedMap.get(APRIL).department()))
                .andExpect(jsonPath("$.[3].sumEconomy").value(expectedMap.get(APRIL).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[3].sumPerPeriod").value(expectedMap.get(APRIL).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[3].transportType").value(expectedMap.get(APRIL).transportType().name()))
                .andExpect(jsonPath("$.[3].percent").value(expectedMap.get(APRIL).percent()))
                .andExpect(jsonPath("$.[4].period").value("MAY"))
                .andExpect(jsonPath("$.[4].department").value(expectedMap.get(MAY).department()))
                .andExpect(jsonPath("$.[4].sumEconomy").value(expectedMap.get(MAY).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[4].sumPerPeriod").value(expectedMap.get(MAY).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[4].transportType").value(expectedMap.get(MAY).transportType().name()))
                .andExpect(jsonPath("$.[4].percent").value(expectedMap.get(MAY).percent()))
                .andExpect(jsonPath("$.[5].period").value("JUNE"))
                .andExpect(jsonPath("$.[5].department").value(expectedMap.get(JUNE).department()))
                .andExpect(jsonPath("$.[5].sumEconomy").value(expectedMap.get(JUNE).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[5].sumPerPeriod").value(expectedMap.get(JUNE).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[5].transportType").value(expectedMap.get(JUNE).transportType().name()))
                .andExpect(jsonPath("$.[5].percent").value(expectedMap.get(JUNE).percent()))
                .andExpect(jsonPath("$.[6].period").value("JULY"))
                .andExpect(jsonPath("$.[6].department").value(expectedMap.get(JULY).department()))
                .andExpect(jsonPath("$.[6].sumEconomy").value(expectedMap.get(JULY).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[6].sumPerPeriod").value(expectedMap.get(JULY).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[6].transportType").value(expectedMap.get(JULY).transportType().name()))
                .andExpect(jsonPath("$.[6].percent").value(expectedMap.get(JULY).percent()))
                .andExpect(jsonPath("$.[7].period").value("AUGUST"))
                .andExpect(jsonPath("$.[7].department").value(expectedMap.get(AUGUST).department()))
                .andExpect(jsonPath("$.[7].sumEconomy").value(expectedMap.get(AUGUST).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[7].sumPerPeriod").value(expectedMap.get(AUGUST).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[7].transportType").value(expectedMap.get(AUGUST).transportType().name()))
                .andExpect(jsonPath("$.[7].percent").value(expectedMap.get(AUGUST).percent()))
                .andExpect(jsonPath("$.[8].period").value("SEPTEMBER"))
                .andExpect(jsonPath("$.[8].department").value(expectedMap.get(SEPTEMBER).department()))
                .andExpect(jsonPath("$.[8].sumEconomy").value(expectedMap.get(SEPTEMBER).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[8].sumPerPeriod").value(expectedMap.get(SEPTEMBER).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[8].transportType").value(expectedMap.get(SEPTEMBER).transportType().name()))
                .andExpect(jsonPath("$.[8].percent").value(expectedMap.get(SEPTEMBER).percent()))
                .andExpect(jsonPath("$.[9].period").value("OCTOBER"))
                .andExpect(jsonPath("$.[9].department").value(expectedMap.get(OCTOBER).department()))
                .andExpect(jsonPath("$.[9].sumEconomy").value(expectedMap.get(OCTOBER).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[9].sumPerPeriod").value(expectedMap.get(OCTOBER).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[9].transportType").value(expectedMap.get(OCTOBER).transportType().name()))
                .andExpect(jsonPath("$.[9].percent").value(expectedMap.get(OCTOBER).percent()))
                .andExpect(jsonPath("$.[10].period").value("NOVEMBER"))
                .andExpect(jsonPath("$.[10].department").value(expectedMap.get(NOVEMBER).department()))
                .andExpect(jsonPath("$.[10].sumEconomy").value(expectedMap.get(NOVEMBER).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[10].sumPerPeriod").value(expectedMap.get(NOVEMBER).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[10].transportType").value(expectedMap.get(NOVEMBER).transportType().name()))
                .andExpect(jsonPath("$.[10].percent").value(expectedMap.get(NOVEMBER).percent()))
                .andExpect(jsonPath("$.[11].period").value("DECEMBER"))
                .andExpect(jsonPath("$.[11].department").value(expectedMap.get(DECEMBER).department()))
                .andExpect(jsonPath("$.[11].sumEconomy").value(expectedMap.get(DECEMBER).sumEconomy().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[11].sumPerPeriod").value(expectedMap.get(DECEMBER).sumPerPeriod().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.[11].transportType").value(expectedMap.get(DECEMBER).transportType().name()))
                .andExpect(jsonPath("$.[11].percent").value(expectedMap.get(DECEMBER).percent()))
        ;
    }

    @Test
    @DisplayName("Получение фактической экономии за год")
    void test_getTransfersFromEconomy() throws Exception {
        var limitId = UUID.randomUUID();
        var year = Instancio.create(Integer.class);

        var history = Instancio.ofList(LimitTransferHistory.class)
                .set(Select.field(LimitTransferHistory::getSourcePeriod), PeriodData.valueOf(Instancio.create(ru.sberbank.ditsib.transport.limits.dto.v2.Month.class).name()))
                .set(Select.field(LimitTransferHistory::getTargetPeriod), PeriodData.valueOf(Instancio.create(ru.sberbank.ditsib.transport.limits.dto.v2.Month.class).name()))
                .create();

        when(limitTransferHistoryService.getHistoryTransfersFromEconomy(limitId, year)).thenReturn(history);

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/economy/%s/past?year=%s".formatted(limitId, year))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        for (var i = 0; i < history.size(); i++) {
            var expected = history.get(i);
            result = result
                    .andExpect(jsonPath("$.[%s].id".formatted(i)).value(expected.getId().toString()))
                    .andExpect(jsonPath("$.[%s].author".formatted(i)).value(expected.getAuthor().toString()))
                    .andExpect(jsonPath("$.[%s].creationTime".formatted(i)).value(expected.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.[%s].sum".formatted(i)).value(expected.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                    .andExpect(jsonPath("$.[%s].year".formatted(i)).value(expected.getYear()))
                    .andExpect(jsonPath("$.[%s].historyType".formatted(i)).value(expected.getHistoryType().name()))
                    .andExpect(jsonPath("$.[%s].source.limit".formatted(i)).value(expected.getSourceLimitId().toString()))
                    .andExpect(jsonPath("$.[%s].source.transportType".formatted(i)).value(expected.getSourceTransportType().name()))
                    .andExpect(jsonPath("$.[%s].source.period".formatted(i)).value(expected.getSourcePeriod().name()))
                    .andExpect(jsonPath("$.[%s].target.limit".formatted(i)).value(expected.getTargetLimitId().toString()))
                    .andExpect(jsonPath("$.[%s].target.transportType".formatted(i)).value(expected.getTargetTransportType().name()))
                    .andExpect(jsonPath("$.[%s].target.period".formatted(i)).value(expected.getTargetPeriod().name()))
            ;
        }
    }

    @TestConfiguration
    @Profile("mock-beans-EconomyControllerV2ImplTest")
    static class TestBeanConfig {

        @Primary
        @Bean
        Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingServicePerPeriodTestBeans(
                LimitSharingPerPeriodService<Period> limitSharingPerPeriodService
        ) {
            return Map.of(MONTHLY, limitSharingPerPeriodService);
        }

    }

}