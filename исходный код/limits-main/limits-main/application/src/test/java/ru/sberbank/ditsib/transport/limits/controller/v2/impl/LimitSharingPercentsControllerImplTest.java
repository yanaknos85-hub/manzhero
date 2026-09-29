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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
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
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPercents;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPercentService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
@DisplayName("Проверка контроллера распределений процентов")
@ActiveProfiles("test")
class LimitSharingPercentsControllerImplTest {

    private static final String USER_ID = "00000000-0000-0000-0000-000000000000";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitSharingPercentService limitSharingService;

    @MockitoBean
    private DepLimitService depLimitService;

    @MockitoBean
    AuthorizationManager<?> authorizationManager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authorizationManager);
    }

    @Test
    @DisplayName("Добавление. Неголовной лимит")
    void test_add_noHeadLimit() throws Exception {
        var limitId = UUID.randomUUID();

        var depLimit = Instancio.of(DepLimit.class)
                .set(Select.field(DepLimit::getParent), Instancio.create(DepLimit.class))
                .create();

        when(depLimitService.get(limitId)).thenReturn(Optional.of(depLimit));

        mockMvc.perform(MockMvcRequestBuilders.post("/sharings/percents")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "january": 1,
                                    "february": 2,
                                    "march": 3,
                                    "april": 4,
                                    "may": 5,
                                    "june": 6,
                                    "july": 7,
                                    "august": 8,
                                    "september": 9,
                                    "october": 10,
                                    "november": 11,
                                    "december": 12,
                                    "limitId": "%s"
                                }""".formatted(limitId))
                )
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("Добавление")
    void test_add() throws Exception {
        var limitId = UUID.randomUUID();

        var depLimit = Instancio.of(DepLimit.class)
                .ignore(Select.field(DepLimit::getParent))
                .set(Select.field(DepLimit::getId), limitId)
                .create();

        when(depLimitService.get(limitId)).thenReturn(Optional.of(depLimit));
        when(limitSharingService.add(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(MockMvcRequestBuilders.post("/sharings/percents")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "january": 1,
                                "february": 2,
                                "march": 3,
                                "april": 4,
                                "may": 5,
                                "june": 6,
                                "july": 7,
                                "august": 8,
                                "september": 9,
                                "october": 10,
                                "november": 11,
                                "december": 12,
                                "limitId": "%s"
                                }
                                """.formatted(limitId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.january").value(1))
                .andExpect(jsonPath("$.february").value(2))
                .andExpect(jsonPath("$.march").value(3))
                .andExpect(jsonPath("$.april").value(4))
                .andExpect(jsonPath("$.may").value(5))
                .andExpect(jsonPath("$.june").value(6))
                .andExpect(jsonPath("$.july").value(7))
                .andExpect(jsonPath("$.august").value(8))
                .andExpect(jsonPath("$.september").value(9))
                .andExpect(jsonPath("$.october").value(10))
                .andExpect(jsonPath("$.november").value(11))
                .andExpect(jsonPath("$.december").value(12))
                .andExpect(jsonPath("$.limitId").value(limitId.toString()))
        ;
    }

    @Test
    @DisplayName("Добавление. Распределение есть")
    void test_add_alreadyExists() throws Exception {
        var limitId = UUID.randomUUID();

        var depLimit = Instancio.of(DepLimit.class)
                .ignore(Select.field(DepLimit::getParent))
                .create();

        var limitSharingPercents = Instancio.create(LimitSharingPercents.class);

        when(depLimitService.get(limitId)).thenReturn(Optional.of(depLimit));
        when(limitSharingService.getByLimit(depLimit)).thenReturn(Optional.of(limitSharingPercents));

        mockMvc.perform(MockMvcRequestBuilders.post("/sharings/percents")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "january": 1,
                                    "february": 2,
                                    "march": 3,
                                    "april": 4,
                                    "may": 5,
                                    "june": 6,
                                    "july": 7,
                                    "august": 8,
                                    "september": 9,
                                    "october": 10,
                                    "november": 11,
                                    "december": 12,
                                    "limitId": "%s"
                                }""".formatted(limitId))
                )
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("Получение одного. Не существует")
    void test_get_notFound() throws Exception {
        var id = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/sharings/percents/" + id)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value(LimitSharingPercents.class.getSimpleName()))
                .andExpect(jsonPath("$.entity.id").value(id.toString()))
        ;
    }

    @Test
    @DisplayName("Изменение. Нет данных")
    void test_edit_notFound() throws Exception {

        var limitId = UUID.randomUUID();
        var limitSharingPercentsId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.put("/sharings/percents/" + limitSharingPercentsId)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "january": 1,
                                    "february": 2,
                                    "march": 3,
                                    "april": 4,
                                    "may": 5,
                                    "june": 6,
                                    "july": 7,
                                    "august": 8,
                                    "september": 9,
                                    "october": 10,
                                    "november": 11,
                                    "december": 12,
                                    "limitId": "%s"
                                }""".formatted(limitId))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value(LimitSharingPercents.class.getSimpleName()))
                .andExpect(jsonPath("$.entity.id").value(limitSharingPercentsId.toString()))
        ;
    }

    @Test
    @DisplayName("Изменение")
    void test_edit() throws Exception {

        var limitId = UUID.randomUUID();
        var limitSharingPercentsId = UUID.randomUUID();

        var limitSharingPercents = Instancio.create(LimitSharingPercents.class);

        when(limitSharingService.get(limitSharingPercentsId)).thenReturn(Optional.of(limitSharingPercents));

        mockMvc.perform(MockMvcRequestBuilders.put("/sharings/percents/" + limitSharingPercentsId)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                     "january": 1,
                                     "february": 2,
                                     "march": 3,
                                     "april": 4,
                                     "may": 5,
                                     "june": 6,
                                     "july": 7,
                                     "august": 8,
                                     "september": 9,
                                     "october": 10,
                                     "november": 11,
                                     "december": 12,
                                     "limitId": "%s"
                                 }""".formatted(limitId))
                )
                .andExpect(status().isOk())
        ;

        var limitSharingPercentsCaptor = ArgumentCaptor.forClass(LimitSharingPercents.class);
        verify(limitSharingService).save(limitSharingPercentsCaptor.capture());

        var actual = limitSharingPercentsCaptor.getValue();
        assertThat(actual).isNotNull();
        assertAll(
                () -> assertThat(actual.getJanuary()).isEqualTo(1),
                () -> assertThat(actual.getFebruary()).isEqualTo(2),
                () -> assertThat(actual.getMarch()).isEqualTo(3),
                () -> assertThat(actual.getApril()).isEqualTo(4),
                () -> assertThat(actual.getMay()).isEqualTo(5),
                () -> assertThat(actual.getJune()).isEqualTo(6),
                () -> assertThat(actual.getJuly()).isEqualTo(7),
                () -> assertThat(actual.getAugust()).isEqualTo(8),
                () -> assertThat(actual.getSeptember()).isEqualTo(9),
                () -> assertThat(actual.getOctober()).isEqualTo(10),
                () -> assertThat(actual.getNovember()).isEqualTo(11),
                () -> assertThat(actual.getDecember()).isEqualTo(12)
        );
    }

    @Test
    @DisplayName("Удаление. Нет данных")
    void test_delete_notFound() throws Exception {
        var limitSharingPercentsId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete("/sharings/percents/" + limitSharingPercentsId)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value(LimitSharingPercents.class.getSimpleName()))
                .andExpect(jsonPath("$.entity.id").value(limitSharingPercentsId.toString()))
        ;
    }

    @Test
    @DisplayName("Удаление")
    void test_delete() throws Exception {
        var limitSharingPercentsId = UUID.randomUUID();

        var limitSharingPercents = Instancio.of(LimitSharingPercents.class)
                .set(Select.field(LimitSharingPercents::getLimit), Instancio.create(DepLimit.class))
                .create();

        when(limitSharingService.get(limitSharingPercentsId)).thenReturn(Optional.of(limitSharingPercents));

        mockMvc.perform(MockMvcRequestBuilders.delete("/sharings/percents/" + limitSharingPercentsId)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk())
        ;

        var limitSharingPercentsCaptor = ArgumentCaptor.forClass(LimitSharingPercents.class);
        verify(limitSharingService).delete(limitSharingPercentsCaptor.capture());

        var actual = limitSharingPercentsCaptor.getValue();
        assertThat(actual).isNotNull();
        assertAll(
                () -> assertThat(actual.getJanuary()).isEqualTo(limitSharingPercents.getJanuary()),
                () -> assertThat(actual.getFebruary()).isEqualTo(limitSharingPercents.getFebruary()),
                () -> assertThat(actual.getMarch()).isEqualTo(limitSharingPercents.getMarch()),
                () -> assertThat(actual.getApril()).isEqualTo(limitSharingPercents.getApril()),
                () -> assertThat(actual.getMay()).isEqualTo(limitSharingPercents.getMay()),
                () -> assertThat(actual.getJune()).isEqualTo(limitSharingPercents.getJune()),
                () -> assertThat(actual.getJuly()).isEqualTo(limitSharingPercents.getJuly()),
                () -> assertThat(actual.getAugust()).isEqualTo(limitSharingPercents.getAugust()),
                () -> assertThat(actual.getSeptember()).isEqualTo(limitSharingPercents.getSeptember()),
                () -> assertThat(actual.getOctober()).isEqualTo(limitSharingPercents.getOctober()),
                () -> assertThat(actual.getNovember()).isEqualTo(limitSharingPercents.getNovember()),
                () -> assertThat(actual.getDecember()).isEqualTo(limitSharingPercents.getDecember()),
                () -> assertThat(actual.getLimit().getId()).isEqualTo(limitSharingPercents.getLimit().getId())
        );
    }

    @Test
    @DisplayName("Получение одного")
    void test_get() throws Exception {
        var limitSharing = Instancio.of(LimitSharingPercents.class)
                .create();

        when(limitSharingService.get(limitSharing.getId())).thenReturn(Optional.of(limitSharing));

        mockMvc.perform(MockMvcRequestBuilders.get("/sharings/percents/" + limitSharing.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(limitSharing.getId().toString()))
                .andExpect(jsonPath("$.author").value(limitSharing.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.creationTime").value(limitSharing.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.january").value(limitSharing.getJanuary()))
                .andExpect(jsonPath("$.february").value(limitSharing.getFebruary()))
                .andExpect(jsonPath("$.march").value(limitSharing.getMarch()))
                .andExpect(jsonPath("$.april").value(limitSharing.getApril()))
                .andExpect(jsonPath("$.may").value(limitSharing.getMay()))
                .andExpect(jsonPath("$.june").value(limitSharing.getJune()))
                .andExpect(jsonPath("$.july").value(limitSharing.getJuly()))
                .andExpect(jsonPath("$.august").value(limitSharing.getAugust()))
                .andExpect(jsonPath("$.september").value(limitSharing.getSeptember()))
                .andExpect(jsonPath("$.october").value(limitSharing.getOctober()))
                .andExpect(jsonPath("$.november").value(limitSharing.getNovember()))
                .andExpect(jsonPath("$.december").value(limitSharing.getDecember()))
        ;
    }

    @Test
    @DisplayName("Получение всех")
    void test_getAll() throws Exception {
        var limitSharings = Instancio.ofList(LimitSharingPercents.class)
                .create();

        when(limitSharingService.getAll(0, 20, Sort.Direction.ASC, null)).thenReturn(new PageImpl<>(limitSharings));

        var expectation = mockMvc.perform(MockMvcRequestBuilders.get("/sharings/percents")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(limitSharings.size()));

        for (var i = 0; i < limitSharings.size(); i++) {
            var expected = limitSharings.get(i);

            expectation
                    .andExpect(jsonPath("$.content.[%s].id".formatted(i)).value(expected.getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].author".formatted(i)).value(expected.getAuthor().getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].creationTime".formatted(i)).value(expected.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content.[%s].january".formatted(i)).value(expected.getJanuary()))
                    .andExpect(jsonPath("$.content.[%s].february".formatted(i)).value(expected.getFebruary()))
                    .andExpect(jsonPath("$.content.[%s].march".formatted(i)).value(expected.getMarch()))
                    .andExpect(jsonPath("$.content.[%s].april".formatted(i)).value(expected.getApril()))
                    .andExpect(jsonPath("$.content.[%s].may".formatted(i)).value(expected.getMay()))
                    .andExpect(jsonPath("$.content.[%s].june".formatted(i)).value(expected.getJune()))
                    .andExpect(jsonPath("$.content.[%s].july".formatted(i)).value(expected.getJuly()))
                    .andExpect(jsonPath("$.content.[%s].august".formatted(i)).value(expected.getAugust()))
                    .andExpect(jsonPath("$.content.[%s].september".formatted(i)).value(expected.getSeptember()))
                    .andExpect(jsonPath("$.content.[%s].october".formatted(i)).value(expected.getOctober()))
                    .andExpect(jsonPath("$.content.[%s].november".formatted(i)).value(expected.getNovember()))
                    .andExpect(jsonPath("$.content.[%s].december".formatted(i)).value(expected.getDecember()))
            ;
        }
    }

}