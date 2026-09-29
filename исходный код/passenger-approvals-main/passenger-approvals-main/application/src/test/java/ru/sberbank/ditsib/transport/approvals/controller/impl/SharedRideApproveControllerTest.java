package ru.sberbank.ditsib.transport.approvals.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.sber.transport.approvals.messaging.ApproveSharedRideMessage;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.ApprovalsApplication;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.SharedRideApproveDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.messaging.resolvers.LimitsDataResolver;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitDTO;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitSharingDTO;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitSharingPerPeriodDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Проверка контроллера согласований присоединения к поездке")
@EmbeddedPostgres
@SpringBootTest(classes = ApprovalsApplication.class)
@Slf4j
class SharedRideApproveControllerTest extends KafkaTest {
    static final String USER_ID_1 = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    static final String USER_ID_2 = "f2459553-9e91-4e32-8e9c-efe4e90c74da";
    static final String APPROVE_BASE_URL = "/shared/ride/approve/";
    static final String DECLINE_BASE_URL = "/shared/ride/decline/";
    static final String LIST_ACTIVE_BASE_URL = "/shared/ride/list/active/";
    static final String LIST_CLOSED_BASE_URL = "/shared/ride/list/closed/";

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SharedRideApprovalRepository approvalRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DelegateRepository delegateRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @MockitoBean
    private LimitsDataResolver limitsDataResolver;
    @MockitoBean("sharedRideApproveOutput")
    private OutputBridge sharedRideApproveOutput;

    private Employee employee1;
    private Employee employee2;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    void beforeEach() {
        var organization = organizationRepository.save(Instancio.of(Organization.class)
                .set(field(Organization::isActive), true)
                .create());
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organization.getId())
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organization.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        employee1 = createEmployee(UUID.fromString(USER_ID_1), department.getId(), position.getId());
        employee2 = createEmployee(UUID.fromString(USER_ID_2), department.getId(), position.getId());
    }

    @AfterEach
    void afterEach() {
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department
                .setDepartmentHeadId(null)
                .setParentId(null)));
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
        delegateRepository.deleteAll();
        approvalRepository.deleteAll();
    }

    private Employee createEmployee(UUID userId, UUID departmentId, UUID positionId) {
        return employeeRepository.save(Instancio.of(Employee.class)
                        .set(field(Employee::getUserId), userId)
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), positionId)
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());
    }

    @DisplayName("Утверждение присоединения к совместной поездки")
    @Test
    void testApprove() throws Exception {
        var approval = createApproval(employee1.getId(), Status.NEW);
        approval = approvalRepository.findById(approval.getId()).orElseThrow();
        assertThat(approval.getStatus()).isEqualTo(Status.NEW);

        mockMvc.perform(
                        put(APPROVE_BASE_URL + approval.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID_1).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        approval = approvalRepository.findById(approval.getId()).orElseThrow();
        assertThat(approval.getStatus()).isEqualTo(Status.APPROVED);
        final var messageCaptor = ArgumentCaptor.forClass(ApproveSharedRideMessage.class);
        verify(sharedRideApproveOutput).send(messageCaptor.capture(), anyMap());
        final var actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.approved()).isTrue();
        assertThat(actualMessage.ownerRequestId()).isEqualTo(approval.getActionId());
        assertThat(actualMessage.addRequestId()).isEqualTo(approval.getAddRequestId());
        assertThat(actualMessage.approvedByEmployeeId()).isEqualTo(employee1.getId());
    }

    @DisplayName("Утверждение/отклонение присоединения к совместной проездке не удается не actor. " +
            "Взывает метод USER_ID_2, а согласование было создано USER_ID_1")
    @Test
    void testForbiddenUser() throws Exception {
        var approval = createApproval(employee1.getId(), Status.NEW);
        approval = approvalRepository.findById(approval.getId()).orElseThrow();
        assertThat(approval.getStatus()).isEqualTo(Status.NEW);

        mockMvc.perform(
                        put(APPROVE_BASE_URL + approval.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID_2).claim("roles", "ROLE_USER"))))
                .andExpect(status().isForbidden()).andReturn();

        approval = approvalRepository.findById(approval.getId()).orElseThrow();
        assertThat(approval.getStatus()).isEqualTo(Status.NEW);

        mockMvc.perform(
                        put(DECLINE_BASE_URL + approval.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID_2).claim("roles", "ROLE_USER"))))
                .andExpect(status().isForbidden()).andReturn();

        approval = approvalRepository.findById(approval.getId()).orElseThrow();
        assertThat(approval.getStatus()).isEqualTo(Status.NEW);
    }

    @DisplayName("Отклонение присоединения к совместной поездки")
    @Test
    void testDecline() throws Exception {
        var approval = createApproval(employee1.getId(), Status.NEW);
        approval = approvalRepository.findById(approval.getId()).orElseThrow();
        assertThat(approval.getStatus()).isEqualTo(Status.NEW);

        mockMvc.perform(
                        put(DECLINE_BASE_URL + approval.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID_1).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        approval = approvalRepository.findById(approval.getId()).orElseThrow();
        final var messageCaptor = ArgumentCaptor.forClass(ApproveSharedRideMessage.class);
        verify(sharedRideApproveOutput).send(messageCaptor.capture(), anyMap());
        final var actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.approved()).isFalse();
        assertThat(actualMessage.ownerRequestId()).isEqualTo(approval.getActionId());
        assertThat(actualMessage.addRequestId()).isEqualTo(approval.getAddRequestId());
        assertThat(actualMessage.approvedByEmployeeId()).isEqualTo(employee1.getId());
    }

    @DisplayName("Несуществующий approve")
    @Test
    void testApproveNotFoundApprove() throws Exception {
        mockMvc.perform(
                        put(APPROVE_BASE_URL + UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID_1).claim("roles", "ROLE_USER"))))
                .andExpect(status().isNotFound()).andReturn();

    }

    @DisplayName("Получение списка активных согласований совместных поездок")
    @Test
    void testGetActiveApprovals() throws Exception {
        GetLimitDTO limit = GetLimitDTO.builder().id(UUID.randomUUID()).year(LocalDate.now().getYear()).build();

        final long balance = 6000L;
        final long sum = 7000L;
        final Long taxiReserved = 700L;
        final Long personalReserved = 300L;

        GetLimitSharingPerPeriodDTO taxiSharingLimitPerPeriod =
                GetLimitSharingPerPeriodDTO.builder().balance(balance).sum(sum).sumReservedForCurrentPeriod(taxiReserved).build();
        GetLimitSharingPerPeriodDTO personalSharingLimitPerPeriod =
                GetLimitSharingPerPeriodDTO.builder().balance(balance).sum(sum).sumReservedForCurrentPeriod(personalReserved).build();

        GetLimitSharingDTO taxiSharingLimit = GetLimitSharingDTO.builder().transportType("TAXI")
                .balance(balance * 12).sum(sum * 12)
                .limitSharingPerPeriodDTO(taxiSharingLimitPerPeriod)
                .build();
        GetLimitSharingDTO personalSharingLimit = GetLimitSharingDTO.builder().transportType("PERSONAL")
                .balance(balance * 12).sum(sum * 12)
                .limitSharingPerPeriodDTO(personalSharingLimitPerPeriod)
                .build();
        List<GetLimitSharingDTO> limitSharing = List.of(taxiSharingLimit, personalSharingLimit);

        //опишем поведение мокнутого feign client
        when(limitsDataResolver.getByDepartmentAndYearAndLimitServiceTypeFull(any(), eq(LocalDate.now().getYear()), any(), any())).thenReturn(limit);
        when(limitsDataResolver.getByLimitFull(any(), any())).thenReturn(limitSharing);

        var approval1 = createApproval(employee1.getId(), Status.NEW);
        var approval2 = createApproval(employee1.getId(), Status.EDITED);
        createApproval(employee1.getId(), Status.CANCELLED);
        createApproval(employee1.getId(), Status.DECLINED);
        createApproval(employee2.getId(), Status.NEW);

        assertThat(approvalRepository.count()).isEqualTo(5);

        when(jwtDecoder.decode(any())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0)).header("algo", "none").jti(USER_ID_1).claim("roles", "ROLE_USER").build());

        MvcResult response = mockMvc.perform(get(LIST_ACTIVE_BASE_URL).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk()).andReturn();
        Collection<SharedRideApproveDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {
                });

        assertThat(actual).hasSize(2);
        assertThat(toIds(actual)).containsExactlyInAnyOrder(approval1.getId(), approval2.getId());
        SharedRideApproveDTO approvalDTO = getDtoAndCheckRequestId(actual, approval1);
        assertThat(approvalDTO.getSumLimit()).isEqualTo((int) sum);
        assertThat(approvalDTO.getRestOfLimit()).isEqualTo((int) balance + personalReserved.intValue());
        approvalDTO = getDtoAndCheckRequestId(actual, approval2);
        assertThat(approvalDTO.getSumLimit()).isEqualTo((int) sum);
        assertThat(approvalDTO.getRestOfLimit()).isEqualTo((int) balance + personalReserved.intValue());
    }

    @DisplayName("Получение списка закрытых согласований совместных поездок")
    @Test
    void testGetClosedApprovals() throws Exception {
        final var employee1 = employeeRepository.findByUserId(UUID.fromString(USER_ID_1)).orElseThrow();
        final var employee2 = employeeRepository.findByUserId(UUID.fromString(USER_ID_2)).orElseThrow();
        createApproval(employee1.getId(), Status.NEW);
        createApproval(employee1.getId(), Status.EDITED);
        var approval3 = createApproval(employee1.getId(), Status.CANCELLED);
        var approval4 = createApproval(employee1.getId(), Status.DECLINED);
        createApproval(employee2.getId(), Status.NEW);

        assertThat(approvalRepository.count()).isEqualTo(5);

        MvcResult response = mockMvc.perform(get(LIST_CLOSED_BASE_URL)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID_1).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        Collection<SharedRideApproveDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {
                });

        assertThat(actual).hasSize(2);
        assertThat(toIds(actual)).containsExactlyInAnyOrder(approval3.getId(), approval4.getId());
        getDtoAndCheckRequestId(actual, approval3);
        getDtoAndCheckRequestId(actual, approval4);
    }

    private SharedRideApproveDTO getDtoAndCheckRequestId(Collection<SharedRideApproveDTO> actual,
                                                         SharedRideJoinApproval approval) {
        var dto = actual.stream().filter(a -> a.getId().equals(approval.getId())).findAny().orElseThrow();
        assertThat(dto.getAddRequestId()).isEqualTo(approval.getAddRequestId());
        return dto;
    }

    private SharedRideJoinApproval createApproval(UUID employeeId, Status status) {
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        var approval = new SharedRideJoinApproval();
        approval.setActionId(UUID.randomUUID());
        approval.setCreationTime(LocalDateTime.now().minusDays(10));
        approval.setAuthorId(UUID.randomUUID());
        approval.setStatus(status);
        approval.setActorId(employeeId);
        approval.setTransportType("PERSONAL");
        approval.setDesiredDate(LocalDateTime.now());
        approval.setPurposeId(tripPurpose.getId());
        approval.setAddRequestId(UUID.randomUUID());
        approval.setApprovedById(employeeId);
        return approvalRepository.save(approval);
    }

    private List<UUID> toIds(Collection<SharedRideApproveDTO> approvals) {
        return approvals.stream().map(TripApproveDTO::getId).toList();
    }

}
