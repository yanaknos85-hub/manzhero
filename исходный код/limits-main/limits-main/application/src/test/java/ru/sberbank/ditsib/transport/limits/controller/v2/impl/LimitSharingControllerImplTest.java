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
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
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
@DisplayName("Проверка контроллера распределений")
@ActiveProfiles("test")
class LimitSharingControllerImplTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final String USER_ID = "00000000-0000-0000-0000-000000000000";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitSharingService limitSharingService;

    @MockitoBean
    AuthorizationManager<?> authorizationManager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authorizationManager);
    }

    @Test
    @DisplayName("Получение одного. Не существует")
    void test_get_notFound() throws Exception {
        var id = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/sharings/" + id)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value(LimitSharing.class.getSimpleName()))
                .andExpect(jsonPath("$.entity.id").value(id.toString()))
        ;
    }

    @Test
    @DisplayName("Получение одного")
    void test_get() throws Exception {
        var limitSharing = Instancio.of(LimitSharing.class)
                .set(Select.field(LimitSharing::getSharingPerPeriods), Instancio.ofList(LimitSharingPerPeriod.class).set(Select.field(LimitSharingPerPeriod::getPeriodData), Instancio.create(PeriodData.class)).create())
                .create();

        when(limitSharingService.get(limitSharing.getId())).thenReturn(Optional.of(limitSharing));

        mockMvc.perform(MockMvcRequestBuilders.get("/sharings/" + limitSharing.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(limitSharing.getId().toString()))
                .andExpect(jsonPath("$.author").value(limitSharing.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.creationTime").value(limitSharing.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.transportType").value(limitSharing.getTransportType().name()))
                .andExpect(jsonPath("$.sum").value(limitSharing.getSum().multiply(BigDecimal.valueOf(100)).intValue()))
                .andExpect(jsonPath("$.balance").value(limitSharing.getBalance().multiply(BigDecimal.valueOf(100)).intValue()))
        ;
    }

    @Test
    @DisplayName("Получение всех")
    void test_getAll() throws Exception {
        var limitSharings = Instancio.ofList(LimitSharing.class)
                .set(Select.field(LimitSharing::getSharingPerPeriods), Instancio.ofList(LimitSharingPerPeriod.class).set(Select.field(LimitSharingPerPeriod::getPeriodData), Instancio.create(PeriodData.class)).create())
                .create();

        when(limitSharingService.getAll(0, 20, Sort.Direction.ASC, null)).thenReturn(new PageImpl<>(limitSharings));

        var expectation = mockMvc.perform(MockMvcRequestBuilders.get("/sharings")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(limitSharings.size()));

        for (var i = 0; i < limitSharings.size(); i++) {
            var expected = limitSharings.get(i);

            expectation
                    .andExpect(jsonPath("$.content.[%s].id".formatted(i)).value(expected.getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].author".formatted(i)).value(expected.getAuthor().getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].creationTime".formatted(i)).value(expected.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content.[%s].transportType".formatted(i)).value(expected.getTransportType().name()))
                    .andExpect(jsonPath("$.content.[%s].sum".formatted(i)).value(expected.getSum().multiply(BigDecimal.valueOf(100)).intValue()))
                    .andExpect(jsonPath("$.content.[%s].balance".formatted(i)).value(expected.getBalance().multiply(BigDecimal.valueOf(100)).intValue()))
            ;
        }
    }

}