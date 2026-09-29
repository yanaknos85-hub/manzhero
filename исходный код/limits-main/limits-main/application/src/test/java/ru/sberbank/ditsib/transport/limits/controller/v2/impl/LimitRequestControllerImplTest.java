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
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestsStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitRequestApproveV2DTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Approver;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitRequestService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
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
@DisplayName("Проверка контроллера заявок")
@ActiveProfiles("test")
class LimitRequestControllerImplTest {

    private static final String USER_ID = "00000000-0000-0000-0000-000000000000";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitRequestService limitRequestService;


    @MockitoBean
    private DiscoveryClient discoveryClient;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    AuthorizationManager<?> authorizationManager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authorizationManager);
    }

    @Test
    @DisplayName("Получение заявки")
    void test_getRequest() throws Exception {
        var limitRequest = Instancio.of(LimitRequest.class)
                .set(Select.field(LimitRequest::getPeriodData), Instancio.create(PeriodData.class))
                .create();
        var requestId = limitRequest.getId();

        when(limitRequestService.get(requestId)).thenReturn(Optional.of(limitRequest));

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/" + requestId)
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(limitRequest.getId().toString()))
                .andExpect(jsonPath("$.humanReadableId").value(limitRequest.getHumanReadableId()))
                .andExpect(jsonPath("$.author.id").value(limitRequest.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.author.humanReadableId").value(limitRequest.getAuthor().getHumanReadableId()))
                .andExpect(jsonPath("$.author.firstName").value(limitRequest.getAuthor().getFirstName()))
                .andExpect(jsonPath("$.author.patronymic").value(limitRequest.getAuthor().getPatronymic()))
                .andExpect(jsonPath("$.author.lastName").value(limitRequest.getAuthor().getLastName()))
                .andExpect(jsonPath("$.author.personnelNumber").value(limitRequest.getAuthor().getPersonnelNumber()))
                .andExpect(jsonPath("$.author.positionId").value(limitRequest.getAuthor().getPositionId().toString()))
                .andExpect(jsonPath("$.author.organizationId").value(limitRequest.getAuthor().getOrganizationId().toString()))
                .andExpect(jsonPath("$.year").value(limitRequest.getYear()))
                .andExpect(jsonPath("$.period").value(limitRequest.getPeriod().name()))
                .andExpect(jsonPath("$.transportType").value(limitRequest.getTransportType().name()))
                .andExpect(jsonPath("$.sum").value(limitRequest.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.description").value(limitRequest.getDescription()))
                .andExpect(jsonPath("$.declineReason").value(limitRequest.getDeclineReason()))
                .andExpect(jsonPath("$.status").value(limitRequest.getStatus().name()))
                .andExpect(jsonPath("$.creationTime").value(limitRequest.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.limitType").value(limitRequest.getLimitType().name()))
                .andExpect(jsonPath("$.askTargets").value(limitRequest.getAskTargets().name()))
                .andExpect(jsonPath("$.approverDtoList.length()").value(limitRequest.getApproverList().size()))
        ;
    }

    @Test
    @DisplayName("Получение заявки. Заявки нет")
    void test_getRequest_notFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get("/requests/" + uuid)
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value(LimitRequest.class.getSimpleName()))
                .andExpect(jsonPath("$.entity.id").value(uuid.toString()))
        ;
    }

    @Test
    @DisplayName("Получение списка заявок")
    void test_getRequest_list() throws Exception {
        var limitRequests = Instancio.ofList(LimitRequest.class)
                .supply(Select.field(LimitRequest::getPeriodData), () -> Instancio.create(PeriodData.class))
                .create();
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getUserId), UUID.fromString(USER_ID))
                .create();

        when(limitRequestService.getAll(null, null, null, null, null, 0, 20, employee)).thenReturn(new PageImpl<>(limitRequests));
        when(employeeService.getByUserId(UUID.fromString(USER_ID))).thenReturn(Optional.of(employee));

        var expectation = mockMvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(limitRequests.size()));
        for (var i = 0; i < limitRequests.size(); i++) {
            var limitRequest = limitRequests.get(i);
            expectation
                    .andExpect(jsonPath("$.content.[%s].humanReadableId".formatted(i)).value(limitRequest.getHumanReadableId()))
                    .andExpect(jsonPath("$.content.[%s].author.id".formatted(i)).value(limitRequest.getAuthor().getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].author.humanReadableId".formatted(i)).value(limitRequest.getAuthor().getHumanReadableId()))
                    .andExpect(jsonPath("$.content.[%s].author.firstName".formatted(i)).value(limitRequest.getAuthor().getFirstName()))
                    .andExpect(jsonPath("$.content.[%s].author.patronymic".formatted(i)).value(limitRequest.getAuthor().getPatronymic()))
                    .andExpect(jsonPath("$.content.[%s].author.lastName".formatted(i)).value(limitRequest.getAuthor().getLastName()))
                    .andExpect(jsonPath("$.content.[%s].author.personnelNumber".formatted(i)).value(limitRequest.getAuthor().getPersonnelNumber()))
                    .andExpect(jsonPath("$.content.[%s].author.positionId".formatted(i)).value(limitRequest.getAuthor().getPositionId().toString()))
                    .andExpect(jsonPath("$.content.[%s].author.organizationId".formatted(i)).value(limitRequest.getAuthor().getOrganizationId().toString()))
                    .andExpect(jsonPath("$.content.[%s].year".formatted(i)).value(limitRequest.getYear()))
                    .andExpect(jsonPath("$.content.[%s].period".formatted(i)).value(limitRequest.getPeriod().name()))
                    .andExpect(jsonPath("$.content.[%s].transportType".formatted(i)).value(limitRequest.getTransportType().name()))
                    .andExpect(jsonPath("$.content.[%s].sum".formatted(i)).value(limitRequest.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                    .andExpect(jsonPath("$.content.[%s].description".formatted(i)).value(limitRequest.getDescription()))
                    .andExpect(jsonPath("$.content.[%s].declineReason".formatted(i)).value(limitRequest.getDeclineReason()))
                    .andExpect(jsonPath("$.content.[%s].status".formatted(i)).value(limitRequest.getStatus().name()))
                    .andExpect(jsonPath("$.content.[%s].creationTime".formatted(i)).value(limitRequest.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content.[%s].limitType".formatted(i)).value(limitRequest.getLimitType().name()))
                    .andExpect(jsonPath("$.content.[%s].askTargets".formatted(i)).value(limitRequest.getAskTargets().name()))
                    .andExpect(jsonPath("$.content.[%s].approverDtoList.length()".formatted(i)).value(limitRequest.getApproverList().size()))
            ;

        }
    }

    @Test
    @DisplayName("Проверка получения статистики по типу транспорта")
    void test_getRequestsByTransportTypeStats() throws Exception {
        var count = 10;
        var stats = Instancio.ofList(GetLimitRequestsStatsV2DTO.class)
                .size(count)
                .create();

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getUserId), UUID.fromString(USER_ID))
                .create();

        when(employeeService.getByUserId(UUID.fromString(USER_ID))).thenReturn(Optional.of(employee));
        when(limitRequestService.getStatistic(anyBoolean(), any())).thenReturn(stats);

        var actual = mockMvc.perform(MockMvcRequestBuilders.get("/requests/stats?active=true")
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var i = 0;
        for (var stat : stats) {
            actual
                    .andExpect(jsonPath("$[%s].transportType".formatted(i)).value(stat.getTransportType().name()))
                    .andExpect(jsonPath("$[%s].count".formatted(i)).value(stat.getCount()));
            i++;
        }
    }

    @Test
    @DisplayName("Проверка отмены")
    void test_cancel() throws Exception {
        var limitRequest = Instancio.of(LimitRequest.class)
                .set(Select.field(LimitRequest::getPeriodData), Instancio.create(PeriodData.class))
                .set(Select.field(LimitRequest::getAuthor), Instancio.of(Employee.class).set(Select.field(Employee::getUserId), UUID.fromString(USER_ID)).create())
                .create();
        var requestId = limitRequest.getId();

        when(limitRequestService.get(requestId)).thenReturn(Optional.of(limitRequest));
        when(limitRequestService.cancel(eq(requestId), anyString())).thenAnswer(inv -> {
            var reason = inv.getArgument(1, String.class);
            limitRequest.setDeclineReason(reason);
            return limitRequest;
        });

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/cancel".formatted(requestId))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "description": "reason"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(limitRequest.getId().toString()))
                .andExpect(jsonPath("$.humanReadableId").value(limitRequest.getHumanReadableId()))
                .andExpect(jsonPath("$.author.id").value(limitRequest.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.author.humanReadableId").value(limitRequest.getAuthor().getHumanReadableId()))
                .andExpect(jsonPath("$.author.firstName").value(limitRequest.getAuthor().getFirstName()))
                .andExpect(jsonPath("$.author.patronymic").value(limitRequest.getAuthor().getPatronymic()))
                .andExpect(jsonPath("$.author.lastName").value(limitRequest.getAuthor().getLastName()))
                .andExpect(jsonPath("$.author.personnelNumber").value(limitRequest.getAuthor().getPersonnelNumber()))
                .andExpect(jsonPath("$.author.positionId").value(limitRequest.getAuthor().getPositionId().toString()))
                .andExpect(jsonPath("$.author.organizationId").value(limitRequest.getAuthor().getOrganizationId().toString()))
                .andExpect(jsonPath("$.year").value(limitRequest.getYear()))
                .andExpect(jsonPath("$.period").value(limitRequest.getPeriod().name()))
                .andExpect(jsonPath("$.transportType").value(limitRequest.getTransportType().name()))
                .andExpect(jsonPath("$.sum").value(limitRequest.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.description").value(limitRequest.getDescription()))
                .andExpect(jsonPath("$.declineReason").value(limitRequest.getDeclineReason()))
                .andExpect(jsonPath("$.status").value(limitRequest.getStatus().name()))
                .andExpect(jsonPath("$.creationTime").value(limitRequest.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.limitType").value(limitRequest.getLimitType().name()))
                .andExpect(jsonPath("$.askTargets").value(limitRequest.getAskTargets().name()))
                .andExpect(jsonPath("$.approverDtoList.length()").value(limitRequest.getApproverList().size()))
        ;
    }

    @Test
    @DisplayName("Проверка отмены. Не автор")
    void test_cancel_nonAuthor() throws Exception {
        var limitRequest = Instancio.of(LimitRequest.class)
                .set(Select.field(LimitRequest::getPeriodData), Instancio.create(PeriodData.class))
                .create();
        var requestId = limitRequest.getId();

        when(limitRequestService.get(requestId)).thenReturn(Optional.of(limitRequest));

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/cancel".formatted(requestId))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "description": "That's I want"
                                }
                                """))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("Проверка отмены. Не найдено")
    void test_cancel_notFound() throws Exception {
        var requestId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/cancel".formatted(requestId))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "sum": 1000,
                                "approvalState": "APPROVED"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value(LimitRequest.class.getSimpleName()))
                .andExpect(jsonPath("$.entity.id").value(requestId.toString()))
        ;
    }

    @Test
    @DisplayName("Проверка согласования. Не найдено")
    void test_approve_notFound() throws Exception {
        var requestId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/approve".formatted(requestId))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "sum": 1000,
                                "approvalState": "APPROVED"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value(LimitRequest.class.getSimpleName()))
                .andExpect(jsonPath("$.entity.id").value(requestId.toString()))
        ;
    }

    @Test
    @DisplayName("Проверка согласования. Не согласовант")
    void test_approve_notApprover() throws Exception {
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getUserId), UUID.fromString(USER_ID))
                .create();
        var limitRequest = Instancio.of(LimitRequest.class)
                .set(Select.field(LimitRequest::getPeriodData), Instancio.create(PeriodData.class))
                .create();

        when(limitRequestService.get(limitRequest.getId())).thenReturn(Optional.of(limitRequest));
        when(limitRequestService.approve(any(LimitRequestApproveV2DTO.class), eq(limitRequest), eq(employee))).thenReturn(limitRequest);
        when(employeeService.getByUserId(UUID.fromString(USER_ID))).thenReturn(Optional.of(employee));

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/approve".formatted(limitRequest.getId()))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "sum": 1000,
                                "approvalState": "APPROVED"
                                }
                                """))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("Проверка согласования. Ожидает согласования")
    void test_approve_waitingForApprove() throws Exception {
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getUserId), UUID.fromString(USER_ID))
                .create();
        var limitRequest = Instancio.of(LimitRequest.class)
                .set(Select.field(LimitRequest::getPeriodData), Instancio.create(PeriodData.class))
                .set(Select.field(LimitRequest::getApproverList), List.of(Approver.builder().approvalState(ApprovalState.AWAITING_APPROVAL).employee(employee).build()))
                .create();

        when(limitRequestService.get(limitRequest.getId())).thenReturn(Optional.of(limitRequest));
        when(employeeService.getByUserId(UUID.fromString(USER_ID))).thenReturn(Optional.of(employee));
        when(limitRequestService.approve(any(LimitRequestApproveV2DTO.class), eq(limitRequest), eq(employee))).thenReturn(limitRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/approve".formatted(limitRequest.getId()))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "sum": 1000,
                                "approvalState": "APPROVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(limitRequest.getId().toString()))
                .andExpect(jsonPath("$.humanReadableId").value(limitRequest.getHumanReadableId()))
                .andExpect(jsonPath("$.author.id").value(limitRequest.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.author.humanReadableId").value(limitRequest.getAuthor().getHumanReadableId()))
                .andExpect(jsonPath("$.author.firstName").value(limitRequest.getAuthor().getFirstName()))
                .andExpect(jsonPath("$.author.patronymic").value(limitRequest.getAuthor().getPatronymic()))
                .andExpect(jsonPath("$.author.lastName").value(limitRequest.getAuthor().getLastName()))
                .andExpect(jsonPath("$.author.personnelNumber").value(limitRequest.getAuthor().getPersonnelNumber()))
                .andExpect(jsonPath("$.author.positionId").value(limitRequest.getAuthor().getPositionId().toString()))
                .andExpect(jsonPath("$.author.organizationId").value(limitRequest.getAuthor().getOrganizationId().toString()))
                .andExpect(jsonPath("$.year").value(limitRequest.getYear()))
                .andExpect(jsonPath("$.period").value(limitRequest.getPeriod().name()))
                .andExpect(jsonPath("$.transportType").value(limitRequest.getTransportType().name()))
                .andExpect(jsonPath("$.sum").value(limitRequest.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.description").value(limitRequest.getDescription()))
                .andExpect(jsonPath("$.declineReason").value(limitRequest.getDeclineReason()))
                .andExpect(jsonPath("$.status").value(limitRequest.getStatus().name()))
                .andExpect(jsonPath("$.creationTime").value(limitRequest.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.limitType").value(limitRequest.getLimitType().name()))
                .andExpect(jsonPath("$.askTargets").value(limitRequest.getAskTargets().name()))
                .andExpect(jsonPath("$.approverDtoList.length()").value(limitRequest.getApproverList().size()))
        ;
    }

    @Test
    @DisplayName("Проверка согласования. Отклонено")
    void test_approve_declined() throws Exception {
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getUserId), UUID.fromString(USER_ID))
                .create();
        var limitRequest = Instancio.of(LimitRequest.class)
                .set(Select.field(LimitRequest::getApproverList), List.of(Approver.builder().approvalState(ApprovalState.DECLINED).employee(employee).build()))
                .set(Select.field(LimitRequest::getPeriodData), Instancio.create(PeriodData.class))
                .create();

        when(limitRequestService.get(limitRequest.getId())).thenReturn(Optional.of(limitRequest));
        when(employeeService.getByUserId(UUID.fromString(USER_ID))).thenReturn(Optional.of(employee));
        when(limitRequestService.approve(any(LimitRequestApproveV2DTO.class), eq(limitRequest), eq(employee))).thenReturn(limitRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/approve".formatted(limitRequest.getId()))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "sum": 1000,
                                "approvalState": "APPROVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(limitRequest.getId().toString()))
                .andExpect(jsonPath("$.humanReadableId").value(limitRequest.getHumanReadableId()))
                .andExpect(jsonPath("$.author.id").value(limitRequest.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.author.humanReadableId").value(limitRequest.getAuthor().getHumanReadableId()))
                .andExpect(jsonPath("$.author.firstName").value(limitRequest.getAuthor().getFirstName()))
                .andExpect(jsonPath("$.author.patronymic").value(limitRequest.getAuthor().getPatronymic()))
                .andExpect(jsonPath("$.author.lastName").value(limitRequest.getAuthor().getLastName()))
                .andExpect(jsonPath("$.author.personnelNumber").value(limitRequest.getAuthor().getPersonnelNumber()))
                .andExpect(jsonPath("$.author.positionId").value(limitRequest.getAuthor().getPositionId().toString()))
                .andExpect(jsonPath("$.author.organizationId").value(limitRequest.getAuthor().getOrganizationId().toString()))
                .andExpect(jsonPath("$.year").value(limitRequest.getYear()))
                .andExpect(jsonPath("$.period").value(limitRequest.getPeriod().name()))
                .andExpect(jsonPath("$.transportType").value(limitRequest.getTransportType().name()))
                .andExpect(jsonPath("$.sum").value(limitRequest.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.description").value(limitRequest.getDescription()))
                .andExpect(jsonPath("$.declineReason").value(limitRequest.getDeclineReason()))
                .andExpect(jsonPath("$.status").value(LimitRequestStatus.CANCELLED.name()))
                .andExpect(jsonPath("$.statusCode").value(LimitRequestStatus.CANCELLED_BY_APPROVERS))
                .andExpect(jsonPath("$.creationTime").value(limitRequest.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.limitType").value(limitRequest.getLimitType().name()))
                .andExpect(jsonPath("$.askTargets").value(limitRequest.getAskTargets().name()))
                .andExpect(jsonPath("$.approverDtoList.length()").value(limitRequest.getApproverList().size()))
        ;
    }

    @Test
    @DisplayName("Проверка согласования. Согласовано полностью")
    void test_approve_fullyApproved() throws Exception {
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getUserId), UUID.fromString(USER_ID))
                .create();
        var limitRequest = Instancio.of(LimitRequest.class)
                .set(Select.field(LimitRequest::getPeriodData), Instancio.create(PeriodData.class))
                .set(Select.field(LimitRequest::getSum), BigDecimal.valueOf(1000))
                .set(Select.field(LimitRequest::getApproverList), List.of(Approver.builder().approvalState(ApprovalState.APPROVED).employee(employee).sum(BigDecimal.valueOf(1000)).build()))
                .create();

        when(limitRequestService.get(limitRequest.getId())).thenReturn(Optional.of(limitRequest));
        when(employeeService.getByUserId(UUID.fromString(USER_ID))).thenReturn(Optional.of(employee));
        when(limitRequestService.approve(any(LimitRequestApproveV2DTO.class), eq(limitRequest), eq(employee))).thenReturn(limitRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/approve".formatted(limitRequest.getId()))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "sum": 1000,
                                "approvalState": "APPROVED"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(limitRequest.getId().toString()))
                .andExpect(jsonPath("$.humanReadableId").value(limitRequest.getHumanReadableId()))
                .andExpect(jsonPath("$.author.id").value(limitRequest.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.author.humanReadableId").value(limitRequest.getAuthor().getHumanReadableId()))
                .andExpect(jsonPath("$.author.firstName").value(limitRequest.getAuthor().getFirstName()))
                .andExpect(jsonPath("$.author.patronymic").value(limitRequest.getAuthor().getPatronymic()))
                .andExpect(jsonPath("$.author.lastName").value(limitRequest.getAuthor().getLastName()))
                .andExpect(jsonPath("$.author.personnelNumber").value(limitRequest.getAuthor().getPersonnelNumber()))
                .andExpect(jsonPath("$.author.positionId").value(limitRequest.getAuthor().getPositionId().toString()))
                .andExpect(jsonPath("$.author.organizationId").value(limitRequest.getAuthor().getOrganizationId().toString()))
                .andExpect(jsonPath("$.year").value(limitRequest.getYear()))
                .andExpect(jsonPath("$.period").value(limitRequest.getPeriod().name()))
                .andExpect(jsonPath("$.transportType").value(limitRequest.getTransportType().name()))
                .andExpect(jsonPath("$.sum").value(limitRequest.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.description").value(limitRequest.getDescription()))
                .andExpect(jsonPath("$.declineReason").value(limitRequest.getDeclineReason()))
                .andExpect(jsonPath("$.status").value(LimitRequestStatus.DONE_FULLY.name()))
                .andExpect(jsonPath("$.creationTime").value(limitRequest.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.limitType").value(limitRequest.getLimitType().name()))
                .andExpect(jsonPath("$.askTargets").value(limitRequest.getAskTargets().name()))
                .andExpect(jsonPath("$.approverDtoList.length()").value(limitRequest.getApproverList().size()))
        ;
    }

    @Test
    @DisplayName("Проверка согласования. Согласовано частично")
    void test_approve_fullyPartially() throws Exception {
        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getUserId), UUID.fromString(USER_ID))
                .create();
        var limitRequest = Instancio.of(LimitRequest.class)
                .set(Select.field(LimitRequest::getApproverList), List.of(Approver.builder().approvalState(ApprovalState.APPROVED).employee(employee).sum(BigDecimal.valueOf(100)).build()))
                .set(Select.field(LimitRequest::getPeriodData), Instancio.create(PeriodData.class))
                .create();

        when(limitRequestService.get(limitRequest.getId())).thenReturn(Optional.of(limitRequest));
        when(employeeService.getByUserId(UUID.fromString(USER_ID))).thenReturn(Optional.of(employee));
        when(limitRequestService.approve(any(LimitRequestApproveV2DTO.class), eq(limitRequest), eq(employee))).thenReturn(limitRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests/%s/approve".formatted(limitRequest.getId()))
                        .header("X-Version", "2")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "sum": 1000,
                                "approvalState": "APPROVED"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(limitRequest.getId().toString()))
                .andExpect(jsonPath("$.humanReadableId").value(limitRequest.getHumanReadableId()))
                .andExpect(jsonPath("$.author.id").value(limitRequest.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.author.humanReadableId").value(limitRequest.getAuthor().getHumanReadableId()))
                .andExpect(jsonPath("$.author.firstName").value(limitRequest.getAuthor().getFirstName()))
                .andExpect(jsonPath("$.author.patronymic").value(limitRequest.getAuthor().getPatronymic()))
                .andExpect(jsonPath("$.author.lastName").value(limitRequest.getAuthor().getLastName()))
                .andExpect(jsonPath("$.author.personnelNumber").value(limitRequest.getAuthor().getPersonnelNumber()))
                .andExpect(jsonPath("$.author.positionId").value(limitRequest.getAuthor().getPositionId().toString()))
                .andExpect(jsonPath("$.author.organizationId").value(limitRequest.getAuthor().getOrganizationId().toString()))
                .andExpect(jsonPath("$.year").value(limitRequest.getYear()))
                .andExpect(jsonPath("$.period").value(limitRequest.getPeriod().name()))
                .andExpect(jsonPath("$.transportType").value(limitRequest.getTransportType().name()))
                .andExpect(jsonPath("$.sum").value(limitRequest.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.description").value(limitRequest.getDescription()))
                .andExpect(jsonPath("$.declineReason").value(limitRequest.getDeclineReason()))
                .andExpect(jsonPath("$.status").value(LimitRequestStatus.DONE_PARTLY.name()))
                .andExpect(jsonPath("$.creationTime").value(limitRequest.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.limitType").value(limitRequest.getLimitType().name()))
                .andExpect(jsonPath("$.askTargets").value(limitRequest.getAskTargets().name()))
                .andExpect(jsonPath("$.approverDtoList.length()").value(limitRequest.getApproverList().size()))
        ;
    }

}