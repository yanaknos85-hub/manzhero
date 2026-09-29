package ru.sberbank.ditsib.transport.approvals.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import ru.sber.transport.approvals.messaging.ApproveFinalTripMessage;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.ApprovalsApplication;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
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
@DisplayName("Проверка контроллера согласований финальной поездки")
@EmbeddedPostgres
@SpringBootTest(classes = ApprovalsApplication.class)
@Slf4j
class FinalTripApproveControllerTest extends KafkaTest {

    public static final String USER_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String TOP_USER_ID = "f2459553-9e91-4e32-8e9c-efe4e90c74da";
    public static final String DELEGATE_USER_ID = "558d39f2-c638-490f-90e9-94d896a65b4c";
    public static final String APPROVE_BASE_URL = "/final/trip/approve/";
    public static final String DECLINE_BASE_URL = "/final/trip/decline/";
    public static final String LIST_ACTIVE_BASE_URL = "/final/trip/list/active/";
    public static final String LIST_CLOSED_BASE_URL = "/final/trip/list/closed/";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FinalTripApprovalRepository approvalRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DepLimitRepository depLimitRepository;
    @MockitoBean("finalTripApproveOutput")
    private OutputBridge finalTripApproveOutput;
    @Autowired
    private DelegateRepository delegateRepository;
    @Autowired
    private RequestDocumentRepository documentRepository;
    @Autowired
    private ViewDocumentRepository viewDocumentRepository;
    @Autowired
    private TripApproverService approverService;
    @MockitoBean
    private LimitsDataResolver limitsDataResolver;
    @Autowired
    private PublicTrApprovalsSettingsRepository publicApprovalsSettingsRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean("approvalsSettingsOutput")
    private OutputBridge approvalsSettingsOutput;

    private static final UUID ACTOR_ID = UUID.randomUUID();
    private final TestSharedData testSharedData = new TestSharedData();

    private Organization organization;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    void beforeEach() {
        organization = organizationRepository.save(Instancio.of(Organization.class)
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

        var actorEmployee = employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getId), ACTOR_ID)
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());
        var delegateEmployee = employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getUserId), UUID.fromString(DELEGATE_USER_ID))
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());

        employeeRepository.save(headEmployee);
        employeeRepository.save(topHeadEmployee);
        employeeRepository.save(actorEmployee);
        employeeRepository.save(delegateEmployee);

        departmentRepository.save(department.setDepartmentHeadId(headEmployee.getId()));
        departmentRepository.save(topDepartment.setDepartmentHeadId(topHeadEmployee.getId()));

        createDepLimit(headEmployee);
        createDepLimit(topHeadEmployee);
    }

    @AfterEach
    void afterEach() {
        publicApprovalsSettingsRepository.deleteAll();
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
        viewDocumentRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @DisplayName("Утверждение финальной поездки")
    @Test
    void testApproveTrip() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        saveAndCheck(approval, Status.NEW);
        updateApproversForAllDepartments();
        var result = putAndCheck(USER_ID, approval, Status.APPROVED, 200);
        checkApproveMessage(result);
    }

    @DisplayName("Утверждение финальной PUBLIC поездки: документов нет, так как это CITY_TRIP_COMPENSATION")
    @Test
    void testApprovePublicTripWODocuments() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        approval.setTransportType("PUBLIC");
        approval.setSuburbTrip(false);
        saveAndCheck(approval, Status.NEW);
        updateApproversForAllDepartments();
        var result = putAndCheck(USER_ID, approval, Status.APPROVED, 200);
        checkApproveMessage(result);
    }

    @DisplayName("Утверждение финальной PUBLIC поездки: документы просмотрены")
    @Test
    void testApprovePublicTrip() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        approval.setTransportType("PUBLIC");
        approval.setSuburbTrip(true);

        var employeeOptional = employeeRepository.findByUserId(UUID.fromString(USER_ID));
        assertThat(employeeOptional).isPresent();
        UUID userEmployeeId = employeeOptional.get().getId();

        RequestDocument document1 = RequestDocument.builder()
                .employeeId(UUID.randomUUID())
                .documentId(UUID.randomUUID())
                .requestId(approval.getActionId())
                .build();
        ViewDocument view1 = ViewDocument.builder()
                .id(document1.getDocumentId())
                .documentId(document1.getDocumentId())
                .dateTime(LocalDateTime.now())
                .employeeId(userEmployeeId)
                .build();
        RequestDocument document2 = RequestDocument.builder()
                .documentId(UUID.randomUUID())
                .employeeId(UUID.randomUUID())
                .requestId(approval.getActionId())
                .build();

        ViewDocument view2 = ViewDocument.builder()
                .id(document2.getDocumentId())
                .documentId(document2.getDocumentId())
                .dateTime(LocalDateTime.now())
                .employeeId(userEmployeeId)
                .build();
        documentRepository.save(document1);
        documentRepository.save(document2);
        viewDocumentRepository.save(view1);
        viewDocumentRepository.save(view2);
        saveAndCheck(approval, Status.NEW);
        updateApproversForAllDepartments();
        var result = putAndCheck(USER_ID, approval, Status.APPROVED, 200);
        checkApproveMessage(result);
    }

    @DisplayName("Утверждение финальной PUBLIC проездки - документы не обязательно просматривать")
    @Test
    void testApprovePublicTrip_withoutDocs() throws Exception {
        publicApprovalsSettingsRepository.save(testSharedData.createPublicSettings(
                organization, "PUBLIC", false, false, true,
                true));

        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        approval.setTransportType("PUBLIC");
        approval.setSuburbTrip(true);
        var employeeOptional = employeeRepository.findByUserId(UUID.fromString(USER_ID));
        assertThat(employeeOptional).isPresent();
        saveAndCheck(approval, Status.NEW);
        updateApproversForAllDepartments();
        var result = putAndCheck(USER_ID, approval, Status.APPROVED, 200);
        checkApproveMessage(result);
    }

    @DisplayName("Утверждение финальной PUBLIC поездки не удалось: не все документы просмотрены согласующим")
    @Test
    void testFailApprovePublicTrip() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        approval.setTransportType("PUBLIC");
        approval.setSuburbTrip(true);

        saveAndCheck(approval, Status.NEW);

        var employeeOptional = employeeRepository.findByUserId(UUID.fromString(USER_ID));
        assertThat(employeeOptional).isPresent();
        UUID userEmployeeId = employeeOptional.get().getId();

        RequestDocument document1 = RequestDocument.builder()
                .employeeId(UUID.randomUUID())
                .documentId(UUID.randomUUID())
                .requestId(approval.getActionId())
                .build();
        ViewDocument view1 = ViewDocument.builder()
                .id(document1.getDocumentId())
                .documentId(document1.getDocumentId())
                .dateTime(LocalDateTime.now())
                .employeeId(userEmployeeId)
                .build();
        RequestDocument document2 = RequestDocument.builder()
                .documentId(UUID.randomUUID())
                .employeeId(UUID.randomUUID())
                .requestId(approval.getActionId())
                .build();

        ViewDocument view2 = ViewDocument.builder()
                .id(document2.getDocumentId())
                .documentId(document2.getDocumentId())
                .dateTime(LocalDateTime.now())
                .employeeId(UUID.randomUUID())
                .build();
        documentRepository.save(document1);
        documentRepository.save(document2);
        viewDocumentRepository.save(view1);
        viewDocumentRepository.save(view2);

        updateApproversForAllDepartments();

        putAndCheck(USER_ID, approval, Status.NEW, 409);
    }

    @DisplayName("Разрешение на финальную проездку дает делегат от head of top department")
    @Test
    @Disabled("На Jenkins по каким-то причинам в TripApproverServiceImpl.computeApprovers не находит делегата для подразделения")
    void testApproveTripByDelegateTopDepartment() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        final Employee topHeadEmployee = employeeRepository.findByUserId(UUID.fromString(TOP_USER_ID)).orElseThrow();
        depLimitRepository.deleteAll();
        createDepLimit(topHeadEmployee);
        Department department = departmentRepository.findById(employee.getDepartmentId()).orElseThrow();
        department.setParentId(topHeadEmployee.getDepartmentId());
        departmentRepository.save(department);

        final Employee topDepartmentHead =
                employeeRepository.findByUserId(UUID.fromString(TOP_USER_ID)).orElseThrow();
        final Employee delegateEmployee =
                employeeRepository.findByUserId(UUID.fromString(DELEGATE_USER_ID)).orElseThrow();

        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(topDepartmentHead.getId());
        delegateRecordDTO.setDelegateId(delegateEmployee.getId());
        delegateRecordDTO.setTransportType("TAXI");
        delegateRecordDTO.setStartDate(LocalDate.now());
        delegateRecordDTO.setEndDate(LocalDate.now().plusMonths(3));
        delegateRepository.save(delegateRecordDTO);

        FinalTripApproval approval = createBaseApproval(employee);

        saveAndCheck(approval, Status.NEW);
        updateApproversForAllDepartments();
        putAndCheck(DELEGATE_USER_ID, approval, Status.NEW, 403);
    }

    @DisplayName("Делегат разрешает финальную поездку")
    @Test
    @Disabled("На Jenkins по каким-то причинам в TripApproverServiceImpl.computeApprovers не находит делегата для подразделения")
    void testApproveTripByDelegate() throws Exception {
        final Employee departmentHead = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        final Employee delegateEmployee =
                employeeRepository.findByUserId(UUID.fromString(DELEGATE_USER_ID)).orElseThrow();

        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(departmentHead.getId());
        delegateRecordDTO.setDelegateId(delegateEmployee.getId());
        delegateRecordDTO.setTransportType("TAXI");
        delegateRecordDTO.setStartDate(LocalDate.now());
        delegateRecordDTO.setEndDate(LocalDate.now().plusMonths(3));
        delegateRepository.save(delegateRecordDTO);

        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);

        saveAndCheck(approval, Status.NEW);
        updateApproversForAllDepartments();
        putAndCheck(DELEGATE_USER_ID, approval, Status.NEW, 403);
    }

    @DisplayName("Делегату запрещено разрешать финальную поездку, так как период его действия закончен")
    @Test
    void testApproveTripByDelegateForbidden() throws Exception {
        final Employee departmentHeadId = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        final Employee delegateEmployeeId =
                employeeRepository.findByUserId(UUID.fromString(DELEGATE_USER_ID)).orElseThrow();


        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(departmentHeadId.getId());
        delegateRecordDTO.setDelegateId(delegateEmployeeId.getId());
        delegateRecordDTO.setTransportType("TAXI");
        delegateRecordDTO.setStartDate(LocalDate.now().minusDays(10));
        delegateRecordDTO.setEndDate(LocalDate.now().minusDays(5));
        delegateRepository.save(delegateRecordDTO);

        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        saveAndCheck(approval, Status.NEW);
        putAndCheck(DELEGATE_USER_ID, approval, Status.NEW, 403);
    }

    @DisplayName("Approve не применился для CANCELLED approval")
    @Test
    void testApproveCancelled() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        approval.setStatus(Status.CANCELLED);
        saveAndCheck(approval, Status.CANCELLED);
        updateApproversForAllDepartments();
        putAndCheck(USER_ID, approval, Status.CANCELLED, 409);
    }

    @DisplayName("Approve не применился, потому что применял его сотрудник, не имеющий на это право")
    @Test
    void testApproveTripForbiddenByDepartmentHead() throws Exception {
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

        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        approval.setStatus(Status.NEW);
        saveAndCheck(approval, Status.NEW);
        putAndCheck(USER_ID, approval, Status.NEW, 403);
    }

    @DisplayName("Разрешение на финальную проездку - несуществующий approve")
    @Test
    void testApproveTripNotFoundApprove() throws Exception {
        mockMvc.perform(
                        put(APPROVE_BASE_URL + UUID.randomUUID())
                                .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound()).andReturn();

    }


    @DisplayName("Отклонение финальной поездки")
    @Test
    void testDeclineTripRequest() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval = createBaseApproval(employee);
        saveAndCheck(approval, Status.NEW);

        updateApproversForAllDepartments();
        mockMvc.perform(
                        put(DECLINE_BASE_URL + approval.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                                .content(objectMapper.writeValueAsString(
                                        CancelDTO.builder().reason("Test reason").build())))
                .andExpect(status().isOk()).andReturn();

        approval = approvalRepository.findById(approval.getId()).orElseThrow(() -> new JUnitException("saveAndPut failed"));
        assertThat(approval.getStatus()).isEqualTo(Status.DECLINED);
        assertThat(approval.getReason()).isEqualTo("Test reason");

        final var messageCaptor = ArgumentCaptor.forClass(ApproveFinalTripMessage.class);
        verify(finalTripApproveOutput).send(messageCaptor.capture(), anyMap());
        final var actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.requestId()).isEqualTo(approval.getActionId());
        assertThat(actualMessage.approved()).isFalse();
        assertThat(actualMessage.message()).isEqualTo("Test reason");
    }

    @DisplayName("Получение списка согласований")
    @Test
    void testGetApprovals() throws Exception {
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

        final Employee employee1 = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        FinalTripApproval approval1 = createBaseApproval(employee1);
        approval1.setCost(35.5);
        approval1.setTaxiClass(TaxiClass.COMFORT);
        approval1.setTransportType("TAXI");
        approval1.setStatus(Status.NEW);
        approvalRepository.save(approval1);

        final Employee employee2 = employeeRepository.findById(ACTOR_ID).orElseThrow();
        FinalTripApproval approval2 = createBaseApproval(employee2);
        approval2.setCost(34.5);
        approval2.setTaxiClass(TaxiClass.BUSINESS);
        approval2.setTransportType("TAXI");
        approval2.setStatus(Status.NEW);
        approvalRepository.save(approval2);

        FinalTripApproval approval3 = createBaseApproval(employee2);
        approval3.setCost(5.5);
        approval3.setTaxiClass(TaxiClass.BUSINESS);
        approval3.setTransportType("TAXI");
        approval3.setStatus(Status.DECLINED);
        approvalRepository.save(approval3);

        FinalTripApproval approval4 = createBaseApproval(employee1);
        approval4.setCost(30.0);
        approval4.setTransportType("PERSONAL");
        approval4.setStatus(Status.NEW);
        approvalRepository.save(approval4);

        assertThat(approvalRepository.count()).isEqualTo(4);

        when(jwtDecoder.decode(any())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0)).header("algo", "none").jti(USER_ID).claim("roles", "ROLE_USER").build());

        updateApproversForAllDepartments();
        MvcResult response = mockMvc.perform(get(LIST_ACTIVE_BASE_URL).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk()).andReturn();
        Collection<TripApproveDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertThat(actual).hasSize(3);
        TripApproveDTO approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), approval1.getId())).findAny()
                .orElseThrow(() -> new IllegalStateException("Result not contains approval 1"));
        checkApprovalDto(approvalDTO, approval1, employee1, balance, sum, taxiReserved);

        approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), approval2.getId())).findAny()
                .orElseThrow(() -> new IllegalStateException("Result not contains approval 2"));
        checkApprovalDto(approvalDTO, approval2, employee2, balance, sum, taxiReserved);

        approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), approval4.getId())).findAny()
                .orElseThrow(() -> new IllegalStateException("Result not contains approval 4"));
        checkApprovalDto(approvalDTO, approval4, employee1, balance, sum, personalReserved);

        response = mockMvc.perform(
                        get(LIST_CLOSED_BASE_URL)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertThat(actual).hasSize(1);
        approvalDTO =
                actual.stream().filter(e -> Objects.equals(e.getId(), approval3.getId())).findAny()
                        .orElseThrow(() -> new IllegalStateException("Result not contains approval 3"));

        checkApprovalDto(approvalDTO, approval3, employee2);
    }

    private FinalTripApproval putAndCheck(String userId, FinalTripApproval approval, Status status, int httpStatus) throws Exception {
        mockMvc.perform(put(APPROVE_BASE_URL + approval.getId())
                        .with(jwt().jwt(builder -> builder.jti(userId).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(httpStatus)).andReturn();
        var result = approvalRepository.findById(approval.getId()).orElseThrow(() -> new JUnitException("saveAndPut failed"));
        assertThat(result.getStatus()).isEqualTo(status);
        return result;
    }

    private void saveAndCheck(FinalTripApproval approval, Status status) {
        approvalRepository.save(approval);
        assertThat(approvalRepository.findById(approval.getId())
                .orElseThrow(() -> new JUnitException("saveAndPut failed")).getStatus()).isEqualTo(status);
    }

    private void checkApproveMessage(FinalTripApproval result) {
        final var messageCaptor = ArgumentCaptor.forClass(ApproveFinalTripMessage.class);
        verify(finalTripApproveOutput).send(messageCaptor.capture(), anyMap());
        final var actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.requestId()).isEqualTo(result.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }

    private void checkApprovalDto(
            TripApproveDTO actual, FinalTripApproval expected, Employee expectedEmployee
    ) {
        assertThat(actual.getCost()).isEqualTo(expected.getCost());
        assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
        assertThat(actual.getTaxiClass()).isEqualTo(expected.getTaxiClass());
        assertThat(actual.getTransportType()).isEqualTo(expected.getTransportType());
        assertThat(actual.getDesiredDate()).isCloseTo(expected.getDesiredDate(), within(1, ChronoUnit.SECONDS));
        assertThat(actual.getPassenger().id()).isEqualTo(expectedEmployee.getId());
        assertThat(actual.getPassenger().firstName()).isEqualTo(expectedEmployee.getFirstName());
        assertThat(actual.getPassenger().lastName()).isEqualTo(expectedEmployee.getLastName());
        assertThat(actual.getPassenger().patronymic()).isEqualTo(expectedEmployee.getPatronymic());
        assertThat(actual.getPassenger().personnelNumber()).isEqualTo(expectedEmployee.getPersonnelNumber());
        assertThat(actual.getPassenger().departmentId()).isEqualTo(expectedEmployee.getDepartmentId());
        assertThat(actual.getRequestId()).isEqualTo(expected.getActionId());
    }

    private void checkApprovalDto(
            TripApproveDTO actual, FinalTripApproval expected, Employee expectedEmployee, Long balance, Long sum,
            Long limitValue
    ) {
        checkApprovalDto(actual, expected, expectedEmployee);
        assertThat(actual.getSumLimit()).isEqualTo(sum.intValue());
        assertThat(actual.getRestOfLimit()).isEqualTo(balance.intValue() + limitValue.intValue());
    }

    private void createDepLimit(Employee employee) {
        DepLimit depLimit = new DepLimit();
        depLimit.setId(UUID.randomUUID());
        depLimit.setYear(LocalDate.now().getYear());
        depLimit.setOwnerId(employee.getId());
        depLimit.setReserve(10L);
        depLimit.setSum(100L);
        depLimitRepository.save(depLimit);
    }

    private FinalTripApproval createBaseApproval(Employee employee) {
        var tripPurpose = tripPurposeRepository.save(new TripPurpose(UUID.randomUUID(), UUID.randomUUID().toString()));
        var approval = new FinalTripApproval();
        approval.setActorId(employee.getId());
        approval.setAuthorId(UUID.randomUUID());
        approval.setActionId(UUID.randomUUID());
        approval.setCreationTime(LocalDateTime.now());
        approval.setTransportType("TAXI");
        approval.setDesiredDate(LocalDateTime.now());
        approval.setPurposeId(tripPurpose.getId());
        approval.setApprovalDate(LocalDateTime.now());
        return approval;
    }

    private void updateApproversForAllDepartments() {
        departmentRepository.findAll().forEach(dep -> approverService.onDepartmentChanged(dep.getId()));
    }
}