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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
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
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitTransferHistory;
import ru.sberbank.ditsib.transport.limits.service.LimitTransferHistoryService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Контроллер истории")
@ActiveProfiles("test")
class LimitHistoryControllerV2ImplTest {

    private final String USER_ID = "00000000-0000-0000-0000-000000000000";

    @MockitoBean
    private JwtDecoder decoder;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private LimitTransferHistoryService limitTransferHistoryService;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager);
    }

    @Test
    @DisplayName("Получение истории. СМД")
    void test_get_data_master() throws Exception {
        var employee = Instancio.create(Employee.class);
        var list = Instancio.ofList(LimitTransferHistory.class)
                .set(Select.field(LimitTransferHistory::getOrganizationId), employee.getOrganizationId())
                .create();

        list.sort(Comparator.comparing(LimitTransferHistory::getCreationTime));

        when(employeeRepository.findByUserId(UUID.fromString(USER_ID))).thenReturn(Optional.of(employee));
        when(limitTransferHistoryService.getAll(null, null,null,0,20, Sort.Direction.ASC)).thenReturn(new PageImpl<>(list));

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/history")
                .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(list.size()))
        ;
        for (var i = 0; i < list.size(); i++) {
            var expected = list.get(i);
            result
                    .andExpect(jsonPath("$.content.[%s].id".formatted(i)).value(expected.getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].author".formatted(i)).value(expected.getAuthor().toString()))
                    .andExpect(jsonPath("$.content.[%s].creationTime".formatted(i)).value(expected.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content.[%s].sum".formatted(i)).value(expected.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                    .andExpect(jsonPath("$.content.[%s].target.transportType".formatted(i)).value(expected.getTargetTransportType().name()))
                    .andExpect(jsonPath("$.content.[%s].target.limit".formatted(i)).value(expected.getTargetLimitId().toString()))
                    .andExpect(jsonPath("$.content.[%s].target.period".formatted(i)).value(expected.getTargetPeriod().name()))
                    .andExpect(jsonPath("$.content.[%s].source.transportType".formatted(i)).value(expected.getSourceTransportType().name()))
                    .andExpect(jsonPath("$.content.[%s].source.limit".formatted(i)).value(expected.getSourceLimitId().toString()))
                    .andExpect(jsonPath("$.content.[%s].source.period".formatted(i)).value(expected.getSourcePeriod().name()))
                    .andExpect(jsonPath("$.content.[%s].historyType".formatted(i)).value(expected.getHistoryType().name()))
                    .andExpect(jsonPath("$.content.[%s].year".formatted(i)).value(expected.getYear()))
                    ;
        }
    }
}