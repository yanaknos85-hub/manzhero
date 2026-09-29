package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.qameta.allure.Feature;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.instancio.Instancio;
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
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitStats;
import ru.sberbank.ditsib.transport.limits.model.limit.Month;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка статистики лимитов")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LimitsStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationManager<?> authManager;

    @MockitoBean
    private LimitStatsService limitStatsService;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authManager);
    }

    @Test
    @DisplayName("Получение общей статистики с параметрами v2")
    void test_get_withParameters_v2() throws Exception {
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organizationId2 = UUID.randomUUID();
        var organizationId3 = UUID.randomUUID();
        var organizations = List.of(organizationId, organizationId2, organizationId3);
        var year = 2023;

        var months = List.of(Month.FEBRUARY, Month.NOVEMBER, Month.DECEMBER, Month.APRIL);
        var transportType = List.of(TransportTypeEnum.TAXI, TransportTypeEnum.DEDICATED, TransportTypeEnum.PERSONAL, TransportTypeEnum.CARSHARING, TransportTypeEnum.DOMESTIC_COURIER);
        var stats = createStats();
        var transportTypeStat = stats.parallelStream().
            collect(Collectors.toMap(LimitStats::transportType, Function.identity(), (l, r) -> new LimitStats<>(l.organizationId(), l.year(), l.period(), l.transportType(), l.budget().add(r.budget()), l.spending().add(r.spending())), LinkedHashMap::new));
        when(limitStatsService.getByOrganizationIdAndYearAndPeriodAndTransportType(organizations, year, months, transportType))
            .thenReturn(stats);

        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/statistic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "organizationId": [
                                        "%s",
                                        "%s",
                                        "%s"
                                    ],
                                    "year": "2023",
                                    "monthList": [
                                        "%s",
                                        "%s",
                                        "%s",
                                        "%s"
                                        ],
                                    "transportType": [
                                        "%s",
                                        "%s",
                                        "%s",
                                        "%s",
                                        "%s"
                                        ]
                                }
                                """.formatted(organizationId.toString(), organizationId2.toString(), organizationId3.toString(),
                                months.getFirst().name(), months.get(1).name(), months.get(2).name(), months.get(3).name(),
                                transportType.getFirst().name(), transportType.get(1).name(), transportType.get(2).name(),
                                transportType.get(3).name(), transportType.get(4).name()))
                    .with(jwt().jwt(builder -> builder.jti(userId.toString()).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpectAll(
                status().isOk(),
                jsonPath("$.charts.length()").value(1),
                jsonPath("$.charts[0].type").value("budget"),
                jsonPath("$.charts[0].data.length()").value(1),
                jsonPath("$.charts[0].data[0].code").value("budget"),
                jsonPath("$.charts[0].data[0].name").value("Лимит"),
                jsonPath("$.charts[0].data[0].transportType").isEmpty(),
                jsonPath("$.charts[0].data[0].totalBudget").value(stats.parallelStream().map(LimitStats::budget).reduce(BigDecimal::add).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(100)).longValue()),
                jsonPath("$.charts[0].data[0].spentBudget").value(stats.parallelStream().map(LimitStats::spending).reduce(BigDecimal::add).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(100)).longValue()),
                jsonPath("$.charts[0].data[0].totalBudgetPerEmployee").value(0),
                jsonPath("$.charts[0].data[0].spentBudgetPerEmployee").value(0),
                jsonPath("$.charts[0].data[0].numOfEmployees").value(0),
                jsonPath("$.charts[0].dataPerTransportType.length()").value(transportTypeStat.keySet().size())
            );

        var keys = new ArrayList<>(transportTypeStat.keySet());
        for (var i = 0; i < keys.size(); i++) {
            var foundTransportType = new AtomicReference<TransportTypeEnum>();
            var expectedData = new AtomicReference<LimitStats<Month>>();
            result.andExpect(
                jsonPath("$.charts[0].dataPerTransportType[%s].transportType".formatted(i)).value(new BaseMatcher() {
                    @Override
                    public boolean matches(Object o) {
                        foundTransportType.set(TransportTypeEnum.valueOf(String.valueOf(o)));
                        expectedData.set(transportTypeStat.get(foundTransportType.get()));
                        return true;
                    }

                    @Override
                    public void describeMismatch(Object o, Description description) {

                    }

                    @Override
                    public void describeTo(Description description) {

                    }
                })).andExpectAll(
                jsonPath("$.charts[0].dataPerTransportType[%s].code".formatted(i)).value("budget_per_transport_type"),
                jsonPath("$.charts[0].dataPerTransportType[%s].name".formatted(i)).value("Лимит по типу транспорта"),
                jsonPath("$.charts[0].dataPerTransportType[%s].transportType".formatted(i)).value(foundTransportType.get().name()),
                jsonPath("$.charts[0].dataPerTransportType[%s].totalBudgetPerEmployee".formatted(i)).value(0),
                jsonPath("$.charts[0].dataPerTransportType[%s].spentBudgetPerEmployee".formatted(i)).value(0),
                jsonPath("$.charts[0].dataPerTransportType[%s].numOfEmployees".formatted(i)).value(0)
            );
        }
    }

    @Test
    @DisplayName("Получение общей статистики с параметрами")
    void test_get_withParameters() throws Exception {
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organizationId2 = UUID.randomUUID();
        var organizationId3 = UUID.randomUUID();
        var organizations = List.of(organizationId, organizationId2, organizationId3);
        var year = 2024;
        var departmentId = UUID.randomUUID();

        var months = List.of(Month.FEBRUARY, Month.NOVEMBER, Month.DECEMBER, Month.APRIL);
        var transportType = List.of(TransportTypeEnum.TAXI, TransportTypeEnum.DEDICATED, TransportTypeEnum.PERSONAL, TransportTypeEnum.CARSHARING, TransportTypeEnum.DOMESTIC_COURIER);
        var stats = createStats();
        var transportTypeStat = stats.parallelStream().
                collect(Collectors.toMap(LimitStats::transportType, Function.identity(), (l, r) -> new LimitStats<>(l.organizationId(), l.year(), l.period(), l.transportType(), l.budget().add(r.budget()), l.spending().add(r.spending())), LinkedHashMap::new));
        when(limitStatsService.getByOrganizationIdAndYearAndPeriodAndTransportType(organizations, year, months, transportType))
                .thenReturn(stats);

        var result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/statistic")
                                .param("organizationId[]", organizationId.toString())
                                .param("organizationId[]", organizationId2.toString())
                                .param("organizationId[]", organizationId3.toString())
                                .param("year", "2024")
                                .param("month[]", months.getFirst().name())
                                .param("month[]", months.get(1).name())
                                .param("month[]", months.get(2).name())
                                .param("month[]", months.get(3).name())
                                .param("transportType[]", transportType.getFirst().name())
                                .param("transportType[]", transportType.get(1).name())
                                .param("transportType[]", transportType.get(2).name())
                                .param("transportType[]", transportType.get(3).name())
                                .param("transportType[]", transportType.get(4).name())
                                .param("departmentId", departmentId.toString())
                                .with(jwt().jwt(builder -> builder.jti(userId.toString()).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.charts.length()").value(1),
                        jsonPath("$.charts[0].type").value("budget"),
                        jsonPath("$.charts[0].data.length()").value(1),
                        jsonPath("$.charts[0].data[0].code").value("budget"),
                        jsonPath("$.charts[0].data[0].name").value("Лимит"),
                        jsonPath("$.charts[0].data[0].transportType").isEmpty(),
                        jsonPath("$.charts[0].data[0].totalBudget").value(stats.parallelStream().map(LimitStats::budget).reduce(BigDecimal::add).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.charts[0].data[0].spentBudget").value(stats.parallelStream().map(LimitStats::spending).reduce(BigDecimal::add).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(100)).longValue()),
                        jsonPath("$.charts[0].data[0].totalBudgetPerEmployee").value(0),
                        jsonPath("$.charts[0].data[0].spentBudgetPerEmployee").value(0),
                        jsonPath("$.charts[0].data[0].numOfEmployees").value(0),
                        jsonPath("$.charts[0].dataPerTransportType.length()").value(transportTypeStat.keySet().size())
                );

        var keys = new ArrayList<>(transportTypeStat.keySet());
        for (var i = 0; i < keys.size(); i++) {
            var foundTransportType = new AtomicReference<TransportTypeEnum>();
            var expectedData = new AtomicReference<LimitStats<Month>>();
            result.andExpect(
                    jsonPath("$.charts[0].dataPerTransportType[%s].transportType".formatted(i)).value(new BaseMatcher() {
                        @Override
                        public boolean matches(Object o) {
                            foundTransportType.set(TransportTypeEnum.valueOf(String.valueOf(o)));
                            expectedData.set(transportTypeStat.get(foundTransportType.get()));
                            return true;
                        }

                        @Override
                        public void describeMismatch(Object o, Description description) {

                        }

                        @Override
                        public void describeTo(Description description) {

                        }
                    })).andExpectAll(
                    jsonPath("$.charts[0].dataPerTransportType[%s].code".formatted(i)).value("budget_per_transport_type"),
                    jsonPath("$.charts[0].dataPerTransportType[%s].name".formatted(i)).value("Лимит по типу транспорта"),
                    jsonPath("$.charts[0].dataPerTransportType[%s].transportType".formatted(i)).value(foundTransportType.get().name()),
                    jsonPath("$.charts[0].dataPerTransportType[%s].totalBudgetPerEmployee".formatted(i)).value(0),
                    jsonPath("$.charts[0].dataPerTransportType[%s].spentBudgetPerEmployee".formatted(i)).value(0),
                    jsonPath("$.charts[0].dataPerTransportType[%s].numOfEmployees".formatted(i)).value(0)
            );
        }
    }

    private List<LimitStats<Month>> createStats() {
        return IntStream.range(0, 100)
            .mapToObj(index -> new LimitStats<>(UUID.randomUUID(), 2024, Instancio.create(Month.class), Instancio.create(TransportTypeEnum.class), Instancio.create(BigDecimal.class), Instancio.create(BigDecimal.class)))
            .toList();
    }

}
