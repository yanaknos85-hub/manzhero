package ru.sberbank.ditsib.transport.approvals.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.JUnitException;
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
import ru.sber.transport.approvals.messaging.ApproveUpdateTripRequestMessage;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.ApprovalsApplication;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.CancelDTO;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.messaging.resolvers.LimitsDataResolver;
import ru.sberbank.ditsib.transport.approvals.services.TripApproverService;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitDTO;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitSharingDTO;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitSharingPerPeriodDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Проверка контроллера согласований изменения поездки")
@EmbeddedPostgres
@SpringBootTest(classes = ApprovalsApplication.class)
class UpdateTripRequestApproveControllerTest extends KafkaTest {
    public static final String USER_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String TOP_USER_ID = "f2459553-9e91-4e32-8e9c-efe4e90c74da";
    public static final String DELEGATE_USER_ID = "558d39f2-c638-490f-90e9-94d896a65b4c";
    public static final String APPROVE_TRIPREQUEST_BASE_URL = "/update/trip/approve/";
    public static final String DECLINE_TRIPREQUEST_BASE_URL = "/update/trip/decline/";
    public static final String LIST_ACTIVE_TRIPREQUEST_BASE_URL = "/update/trip/list/active/";
    public static final String LIST_CLOSED_TRIPREQUEST_BASE_URL = "/update/trip/list/closed/";
    public static final String BASE_URL = "/update/trip/";

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean("updateTripApproveOutput")
    private OutputBridge updateTripApproveOutput;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UpdateTripRequestApprovalRepository approvalRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepLimitRepository depLimitRepository;
    @Autowired
    private DelegateRepository delegateRepository;
    @Autowired
    private TripApproverService approverService;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @MockitoBean
    private LimitsDataResolver limitsDataResolver;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    private static final UUID ACTOR_ID = UUID.randomUUID();

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
        var departmentId = UUID.randomUUID();
        var topDepartmentId = UUID.randomUUID();
        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getId), departmentId)
                .set(field(Department::getOrganizationId), organization.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        var topDepartment = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getId), topDepartmentId)
                .set(field(Department::getOrganizationId), organization.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        var topHeadEmployee = employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), topDepartmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .set(field(Employee::getUserId), UUID.fromString(TOP_USER_ID))
                .create());
        var headEmployee = employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .set(field(Employee::getUserId), UUID.fromString(USER_ID))
                .create());

        employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getId), ACTOR_ID)
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());
        employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getUserId), UUID.fromString(DELEGATE_USER_ID))
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());

        departmentRepository.save(department.setDepartmentHeadId(headEmployee.getId()));
        departmentRepository.save(topDepartment.setDepartmentHeadId(topHeadEmployee.getId()));

        createDepLimit(headEmployee);
        createDepLimit(topHeadEmployee);
    }

    @AfterEach
    void afterEach() {
        approvalRepository.deleteAll();
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department
                .setDepartmentHeadId(null)
                .setParentId(null)));
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
        delegateRepository.deleteAll();
        depLimitRepository.deleteAll();
    }

    @DisplayName("Разрешение на проездку")
    @Test
    void testApproveTripRequest() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        UpdateTripRequestApproval approval = createApproval(employee);
        approveAndCheckTrip(approval, USER_ID);

    }

    @DisplayName("Разрешение на проездку дает head of top department")
    @Test
    void testApproveTripRequestByTopDepartment() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        final Employee topHeadEmployee = getEmployee(TOP_USER_ID);
        depLimitRepository.deleteAll();
        createDepLimit(topHeadEmployee);
        Department department = departmentRepository.findById(employee.getDepartmentId()).orElseThrow();
        department.setParentId(topHeadEmployee.getDepartmentId());
        departmentRepository.save(department);

        UpdateTripRequestApproval approval = createApproval(employee);
        saveAndCheck(approval, Status.NEW);
        updateApproversForAllDepartments();
        putAndCheck(TOP_USER_ID, approval, Status.NEW, 403);
    }

    @DisplayName("Разрешение на проездку дает делегат от head of top department")
    @Test
    @Disabled("На Jenkins по каким-то причинам в TripApproverServiceImpl.computeApprovers не находит делегата для подразделения")
    void testApproveTripRequestByDelegateTopDepartment() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        final Employee topHeadEmployee = getEmployee(TOP_USER_ID);
        depLimitRepository.deleteAll();
        createDepLimit(topHeadEmployee);
        Department department = departmentRepository.findById(employee.getDepartmentId()).orElseThrow();
        department.setParentId(topHeadEmployee.getDepartmentId());
        departmentRepository.save(department);

        final Employee topDepartmentHead =
                getEmployee(TOP_USER_ID);
        final Employee delegateEmployee =
                getEmployee(DELEGATE_USER_ID);

        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(topDepartmentHead.getId());
        delegateRecordDTO.setDelegateId(delegateEmployee.getId());
        delegateRecordDTO.setTransportType("TAXI");
        delegateRecordDTO.setStartDate(LocalDate.now());
        delegateRecordDTO.setEndDate(LocalDate.now().plusMonths(3));
        delegateRepository.save(delegateRecordDTO);

        UpdateTripRequestApproval approval = createApproval(employee);

        approveAndCheckTrip(approval, DELEGATE_USER_ID);
    }

    @DisplayName("Делегат разрешает поездку")
    @Test
    @Disabled("На Jenkins по каким-то причинам в TripApproverServiceImpl.computeApprovers не находит делегата для подразделения")
    void testApproveTripRequestByDelegate() throws Exception {
        final Employee departmentHead = getEmployee(USER_ID);
        final Employee delegateEmployee =
                getEmployee(DELEGATE_USER_ID);

        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(departmentHead.getId());
        delegateRecordDTO.setDelegateId(delegateEmployee.getId());
        delegateRecordDTO.setTransportType("TAXI");
        delegateRecordDTO.setStartDate(LocalDate.now());
        delegateRecordDTO.setEndDate(LocalDate.now().plusMonths(3));
        delegateRepository.save(delegateRecordDTO);

        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        UpdateTripRequestApproval approval = createApproval(employee);

        approveAndCheckTrip(approval, DELEGATE_USER_ID);
    }

    @DisplayName("Делегату запрещено разрешать поездку, так как период его действия закончен")
    @Test
    void testApproveTripRequestByDelegateForbidden() throws Exception {
        final Employee departmentHeadId = getEmployee(USER_ID);
        final Employee delegateEmployeeId =
                getEmployee(DELEGATE_USER_ID);


        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(departmentHeadId.getId());
        delegateRecordDTO.setDelegateId(delegateEmployeeId.getId());
        delegateRecordDTO.setTransportType("TAXI");
        delegateRecordDTO.setStartDate(LocalDate.now().minusDays(10));
        delegateRecordDTO.setEndDate(LocalDate.now().minusDays(5));
        delegateRepository.save(delegateRecordDTO);

        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        UpdateTripRequestApproval approval = createApproval(employee);

        saveAndCheck(approval, Status.NEW);
        putAndCheck(DELEGATE_USER_ID, approval, Status.NEW, 403);
    }

    @DisplayName("Approve не применился для CANCELLED approval")
    @Test
    void testApproveCancelledTripRequest() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        UpdateTripRequestApproval approval = createApproval(employee);
        approval.setStatus(Status.CANCELLED);
        saveAndCheck(approval, Status.CANCELLED);
        updateApproversForAllDepartments();
        putAndCheck(USER_ID, approval, Status.CANCELLED, 409);

    }

    @DisplayName("Approve не применился, потому что применял его сотрудник, не имеющий на это право")
    @Test
    void testApproveTripRequestForbiddenByDepartmentHead() throws Exception {
        var department = departmentRepository.findAll().get(0);
        var organizationId = department.getOrganizationId();
        var position = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organizationId)
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var departmentHead = employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());
        departmentRepository.save(department.setDepartmentHeadId(departmentHead.getId()));

        final var employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        var approval = createApproval(employee);
        approval.setStatus(Status.NEW);

        saveAndCheck(approval, Status.NEW);
        putAndCheck(USER_ID, approval, Status.NEW, 403);

    }

    @DisplayName("Разрешение на проездку - несуществующий approve")
    @Test
    void testApproveTripNotFoundApprove() throws Exception {
        mockMvc.perform(
                        put(APPROVE_TRIPREQUEST_BASE_URL + UUID.randomUUID())
                                .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound()).andReturn();

    }


    @DisplayName("Отклонение поездки")
    @Test
    void testDeclineTripRequest() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        UpdateTripRequestApproval approval = createApproval(employee);
        saveAndCheck(approval, Status.NEW);

        updateApproversForAllDepartments();
        mockMvc.perform(
                        put(DECLINE_TRIPREQUEST_BASE_URL + approval.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                                .content(objectMapper.writeValueAsString(
                                        CancelDTO.builder().reason("Test reason").build())))
                .andExpect(status().isOk()).andReturn();

        approval = approvalRepository.findById(approval.getId()).orElseThrow(() -> new JUnitException("saveAndPut failed"));
        assertThat(approval.getReason()).isEqualTo("Test reason");
        assertThat(approval.getStatus()).isEqualTo(Status.DECLINED);
        final var messageCaptor = ArgumentCaptor.forClass(ApproveUpdateTripRequestMessage.class);
        verify(updateTripApproveOutput).send(messageCaptor.capture(), anyMap());
        final var actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.updateId()).isEqualTo(approval.getUpdateId());
        assertThat(actualMessage.approved()).isFalse();
        assertThat(actualMessage.message()).isEqualTo("Test reason");
    }

    @DisplayName("Получение списка согласований")
    @Test
    void testGetApprovals() throws Exception {
        GetLimitDTO limit = GetLimitDTO.builder().id(UUID.randomUUID()).year(LocalDate.now().getYear()).build();

        final long balance = 6000L;
        final Long sum = 7000L;
        final long taxiReserved = 700L;
        final long personalReserved = 300L;

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

        final Employee employee1 = getEmployee(USER_ID);
        UpdateTripRequestApproval tripApproval1 = createApproval(employee1);
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        tripApproval1.setCost(2.3);
        tripApproval1.setPurposeId(tripPurpose.getId());
        tripApproval1.setTaxiClass(TaxiClass.COMFORT);
        tripApproval1.setTransportType("TAXI");
        tripApproval1.setStatus(Status.NEW);
        approvalRepository.save(tripApproval1);

        final Employee employee2 = employeeRepository.findById(ACTOR_ID).orElseThrow();
        UpdateTripRequestApproval tripApproval2 = createApproval(employee2);
        tripApproval2.setCost(5);
        tripApproval2.setPurposeId(tripPurpose.getId());
        tripApproval2.setTaxiClass(TaxiClass.BUSINESS);
        tripApproval2.setTransportType("TAXI");
        tripApproval2.setStatus(Status.NEW);
        approvalRepository.save(tripApproval2);

        UpdateTripRequestApproval tripApproval3 = createApproval(employee2);
        tripApproval3.setCost(5);
        tripApproval3.setPurposeId(tripPurpose.getId());
        tripApproval3.setTaxiClass(TaxiClass.BUSINESS);
        tripApproval3.setTransportType("TAXI");
        tripApproval3.setStatus(Status.DECLINED);
        approvalRepository.save(tripApproval3);

        UpdateTripRequestApproval tripApproval4 = createApproval(employee2);
        tripApproval4.setCost(5);
        tripApproval4.setPurposeId(tripPurpose.getId());
        tripApproval4.setTransportType("PERSONAL");
        tripApproval4.setStatus(Status.NEW);
        approvalRepository.save(tripApproval4);

        assertThat(approvalRepository.count()).isEqualTo(4);

        when(jwtDecoder.decode(any())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0)).header("algo", "none").jti(USER_ID).claim("roles", "ROLE_USER").build());

        updateApproversForAllDepartments();
        MvcResult response = mockMvc.perform(get(LIST_ACTIVE_TRIPREQUEST_BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        Collection<TripApproveDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertThat(actual).hasSize(3);

        TripApproveDTO approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval1.getId())).findAny()
                .orElseThrow(() -> new IllegalStateException("Result not contains approval 1"));

        checkApprovalDto(approvalDTO, tripApproval1, employee1, sum, balance + taxiReserved);

        approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval2.getId())).findAny()
                .orElseThrow(() -> new IllegalStateException("Result not contains approval 2"));
        checkApprovalDto(approvalDTO, tripApproval2, employee2, sum, balance + taxiReserved);

        approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval4.getId())).findAny()
                .orElseThrow(() -> new IllegalStateException("Result not contains approval 4"));
        checkApprovalDto(approvalDTO, tripApproval4, employee2, sum, balance + personalReserved);

        response = mockMvc.perform(get(LIST_CLOSED_TRIPREQUEST_BASE_URL)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        actual = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(actual).hasSize(1);
        approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval3.getId())).findAny()
                .orElseThrow(() -> new IllegalStateException("Result not contains approval 3"));
        checkApprovalDto(approvalDTO, tripApproval3, employee2);
    }

    @DisplayName("Получение согласования по ID заявки")
    @Test
    void getApprovalByRequestId() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        UpdateTripRequestApproval approval = createApproval(employee);

        saveAndCheck(approval, Status.NEW);

        approvalRepository.save(approval);
        var updateTripRequestApprovalOptional = approvalRepository.findById(approval.getId());
        assertThat(updateTripRequestApprovalOptional).isPresent();
        approval = updateTripRequestApprovalOptional.get();
        String url = BASE_URL + "/" + approval.getActionId();
        updateApproversForAllDepartments();

        var response = mockMvc.perform(get(url)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), TripApproveDTO.class);
        assertThat(actual.getId()).isEqualTo(approval.getId());
        assertThat(actual.getRequestId()).isEqualTo(approval.getActionId());
        assertThat(actual.getStatus().name()).isEqualTo(Status.NEW.name());
    }

    @DisplayName("Получение согласования по ID заявки - несуществующая заявка - null")
    @Test
    void getApprovalByRequestId_exception() throws Exception {
        String url = BASE_URL + "/" + UUID.randomUUID();
        var ex = mockMvc.perform(get(url)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }

    private Employee getEmployee(String userId) {
        return employeeRepository.findByUserId(UUID.fromString(userId)).orElseThrow();
    }

    private void approveAndCheckTrip(UpdateTripRequestApproval approval, String userId) throws Exception {
        saveAndCheck(approval, Status.NEW);
        updateApproversForAllDepartments();
        var result = putAndCheck(userId, approval, Status.APPROVED, 200);
        checkApproveMessage(result, userId);
    }

    private UpdateTripRequestApproval putAndCheck(String userId, UpdateTripRequestApproval approval, Status status, int httpStatus) throws Exception {
        mockMvc.perform(put(APPROVE_TRIPREQUEST_BASE_URL + approval.getId())
                        .with(jwt().jwt(builder -> builder.jti(userId).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(httpStatus)).andReturn();
        var result = approvalRepository.findById(approval.getId()).orElseThrow(() -> new JUnitException("saveAndPut failed"));
        assertThat(result.getStatus()).isEqualTo(status);
        return result;
    }

    private void saveAndCheck(UpdateTripRequestApproval approval, Status status) {
        approvalRepository.save(approval);
        assertThat(approvalRepository.findById(approval.getId())
                .orElseThrow(() -> new JUnitException("saveAndPut failed")).getStatus()).isEqualTo(status);
    }

    private void checkApproveMessage(UpdateTripRequestApproval result, String userId) {
        final var messageCaptor = ArgumentCaptor.forClass(ApproveUpdateTripRequestMessage.class);
        verify(updateTripApproveOutput).send(messageCaptor.capture(), anyMap());
        final var actualMessage = messageCaptor.getValue();
        Employee callEmployee = getEmployee(userId);
        assertThat(actualMessage.updateId()).isEqualTo(result.getUpdateId());
        assertThat(actualMessage.approvedByEmployeeId()).isEqualTo(callEmployee.getId());
        assertThat(actualMessage.approved()).isTrue();
    }

    private void createDepLimit(Employee employee) {
        var depLimit = new DepLimit();
        depLimit.setId(UUID.randomUUID());
        depLimit.setYear(LocalDate.now().getYear());
        depLimit.setOwnerId(employee.getId());
        depLimit.setReserve(10L);
        depLimit.setSum(100L);
        depLimitRepository.save(depLimit);
    }

    private UpdateTripRequestApproval createApproval(Employee employee) {
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        var approval = new UpdateTripRequestApproval();
        approval.setActorId(employee.getId());
        approval.setAuthorId(UUID.randomUUID());
        approval.setActionId(UUID.randomUUID());
        approval.setCreationTime(LocalDateTime.now());
        approval.setTransportType("TAXI");
        approval.setUpdateId(UUID.randomUUID());
        approval.setDesiredDate(LocalDateTime.now());
        approval.setPurposeId(tripPurpose.getId());
        return approval;
    }

    private void updateApproversForAllDepartments() {
        departmentRepository.findAll().forEach(dep -> approverService.onDepartmentChanged(dep.getId()));
    }

    private void checkApprovalDto(TripApproveDTO actual, UpdateTripRequestApproval expected, Employee expectedEmployee) {
        assertThat(actual.getCost()).isEqualTo(expected.getCost());
        assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
        assertThat(actual.getTaxiClass()).isEqualTo(expected.getTaxiClass());
        assertThat(actual.getTransportType()).isEqualTo(expected.getTransportType());
        assertThat(actual.getDesiredDate()).isCloseTo(expected.getDesiredDate(), within(1, ChronoUnit.MINUTES));
        assertThat(actual.getPassenger().id()).isEqualTo(expectedEmployee.getId());
        assertThat(actual.getPassenger().firstName()).isEqualTo(expectedEmployee.getFirstName());
        assertThat(actual.getPassenger().lastName()).isEqualTo(expectedEmployee.getLastName());
        assertThat(actual.getPassenger().patronymic()).isEqualTo(expectedEmployee.getPatronymic());
        assertThat(actual.getPassenger().personnelNumber()).isEqualTo(expectedEmployee.getPersonnelNumber());
        assertThat(actual.getPassenger().departmentId()).isEqualTo(expectedEmployee.getDepartmentId());
        assertThat(actual.getRequestId()).isEqualTo(expected.getActionId());
    }

    private void checkApprovalDto(
            TripApproveDTO actual, UpdateTripRequestApproval expected, Employee expectedEmployee, Long sum, Long restOfLimit
    ) {
        checkApprovalDto(actual, expected, expectedEmployee);
        assertThat(actual.getSumLimit()).isEqualTo(sum);
        assertThat(actual.getRestOfLimit()).isEqualTo(restOfLimit);
    }
}
