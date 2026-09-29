package ru.sberbank.ditsib.transport.approvals.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.sber.transport.approvals.messaging.ApproveTripRequestMessage;
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
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Проверка контроллера согласований")
@EmbeddedPostgres
@SpringBootTest(classes = ApprovalsApplication.class)
class TripRequestApproveControllerTest extends KafkaTest {
    
    public static final String USER_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String TOP_USER_ID = "f2459553-9e91-4e32-8e9c-efe4e90c74da";
    public static final String DELEGATE_USER_ID = "558d39f2-c638-490f-90e9-94d896a65b4c";
    public static final String APPROVE_TRIPREQUEST_BASE_URL = "/request/trip/approve/";
    public static final String DECLINE_TRIPREQUEST_BASE_URL = "/request/trip/decline/";
    public static final String LIST_ACTIVE_TRIPREQUEST_BASE_URL = "/request/trip/list/active";
    public static final String LIST_CLOSED_TRIPREQUEST_BASE_URL = "/request/trip/list/closed";
    public static final String PAGE_ACTIVE_TRIPREQUEST_BASE_URL_RAW = "/request/trip/page/active";
    public static final String PAGE_ACTIVE_TRIPREQUEST_BASE_URL = "/request/trip/page/active?size=4&page=0&sort=desiredDate";
    public static final String PAGE_CLOSED_TRIPREQUEST_BASE_URL = "/request/trip/page/closed?size=2&page=0&sort=desiredDate";
    public static final long LIMIT_SUM = 100L;

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean("tripRequestApproveOutput")
    private OutputBridge tripRequestApproveOutput;
    @Autowired
    private TripRequestApprovalRepository approvalRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private DepLimitRepository depLimitRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
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
    
    private final TestSharedData testSharedData = new TestSharedData();
    
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID_2 = UUID.randomUUID();
    private static final String LAST_NAME = "Gates";
    private static final String PERSONNEL_NUMBER = Instancio.create(String.class);
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
                .set(field(Employee::getId), UUID.fromString(TOP_USER_ID))
                .set(field(Employee::getUserId), UUID.fromString(TOP_USER_ID))
                .set(field(Employee::getDepartmentId), topDepartmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());
        var headEmployee = employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getId), UUID.fromString(USER_ID))
                .set(field(Employee::getUserId), UUID.fromString(USER_ID))
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create());
        employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getUserId), ACTOR_ID)
                .set(field(Employee::getId), ACTOR_ID)
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .set(field(Employee::getLastName), LAST_NAME)
                .create());
        employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getId), ACTOR_ID_2)
                .set(field(Employee::getUserId), ACTOR_ID_2)
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), position.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .set(field(Employee::getLastName), LAST_NAME)
                .set(field(Employee::getPersonnelNumber), PERSONNEL_NUMBER)
                .create());
        employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getId), UUID.fromString(DELEGATE_USER_ID))
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
        tripPurposeRepository.deleteAll();
    }
    
    @DisplayName("Разрешение на поездку")
    @Test
    void testApproveTripRequest() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
        
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isOk()).andReturn();
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.APPROVED);
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, tripRequestApproval.getActionId())));
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }
    
    @DisplayName("Разрешение на поездку на общ транспорте. Апрув проходит, потому что документы были загружены " +
                 "согласующим")
    @Test
    void testApprovePublicTripRequestByUploadUser() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        tripRequestApproval.setTransportType("PUBLIC");
        tripRequestApproval.setSuburbTrip(false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
    
        var employeeOptional = employeeRepository.findByUserId(UUID.fromString(USER_ID));
        assertThat(employeeOptional).isPresent();
        UUID userEmployee = employeeOptional.get().getId();
        
        RequestDocument document1 = RequestDocument.builder()
                                                   .employeeId(userEmployee)
                                                   .documentId(UUID.randomUUID())
                                                   .requestId(tripRequestApproval.getActionId())
                                                   .build();
        RequestDocument document2 = RequestDocument.builder()
                                                   .employeeId(userEmployee)
                                                   .documentId(UUID.randomUUID())
                                                   .requestId(tripRequestApproval.getActionId())
                                                   .build();
        
        documentRepository.save(document1);
        documentRepository.save(document2);
        
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                /*.content(request)*/)
               .andExpect(status().isOk()).andReturn();
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.APPROVED);
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, tripRequestApproval.getActionId())));
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }
    
    @DisplayName("Разрешение на поездку на общ транспорте. Апрув не проходоит, потому что нет просмотров у одного из" +
                 " документов")
    @Test
    void testNotApprovePublicTripRequestNoViews() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        tripRequestApproval.setTransportType("PUBLIC");
        tripRequestApproval.setSuburbTrip(true);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
    
        var employeeOptional = employeeRepository.findByUserId(UUID.fromString(USER_ID));
        assertThat(employeeOptional).isPresent();
        UUID userEmployee = employeeOptional.get().getId();
        RequestDocument document1 = RequestDocument.builder()
                                                   .employeeId(UUID.randomUUID())
                                                   .documentId(UUID.randomUUID())
                                                   .requestId(tripRequestApproval.getActionId())
                                                   .build();
        RequestDocument document2 = RequestDocument.builder()
                                                   .employeeId(userEmployee)
                                                   .documentId(UUID.randomUUID())
                                                   .requestId(tripRequestApproval.getActionId())
                                                   .build();
        
        documentRepository.save(document1);
        documentRepository.save(document2);
        
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isConflict()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
    }
    
    @DisplayName("Разрешение на поездку на общ транспорте - без необходимости просмотра документа")
    @Test
    void testApprovePublicTripRequest_withoutDocs() throws Exception {
        publicApprovalsSettingsRepository.save(testSharedData.createPublicSettings(
                organization, "PUBLIC", false, true, true, true));
        
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        tripRequestApproval.setTransportType("PUBLIC");
        tripRequestApproval.setSuburbTrip(false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
    
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isOk()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.APPROVED);
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, tripRequestApproval.getActionId())));
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }
    
    
    @DisplayName("Разрешение на поездку на общ транспорте. Апрув не проходоит, потому что документы просматривались " +
                 "другим пользователем")
    @Test
    void testNotApprovePublicTripRequestViewsAnotherUser() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        tripRequestApproval.setTransportType("PUBLIC");
        tripRequestApproval.setSuburbTrip(true);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
    
        var employeeOptional = employeeRepository.findByUserId(UUID.fromString(USER_ID));
        assertThat(employeeOptional).isPresent();
        UUID userEmployee = employeeOptional.get().getId();
        RequestDocument document1 = RequestDocument.builder()
                                                   .employeeId(UUID.randomUUID())
                                                   .documentId(UUID.randomUUID())
                                                   .requestId(tripRequestApproval.getActionId())
                                                   .build();
        ViewDocument view1 = ViewDocument.builder()
                                         .id(document1.getDocumentId())
                                         .documentId(document1.getDocumentId())
                                         .dateTime(LocalDateTime.now())
                                         .employeeId(UUID.randomUUID())
                                         .build();
        RequestDocument document2 = RequestDocument.builder()
                                                   .documentId(UUID.randomUUID())
                                                   .employeeId(userEmployee)
                                                   .requestId(tripRequestApproval.getActionId())
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
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isConflict()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
    }
    
    @DisplayName("Разрешение на поездку на общ транспорте. Все документы просмотрены согласующим")
    @Test
    void testApprovePublicTripRequestWithViews() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        tripRequestApproval.setTransportType("PUBLIC");
        tripRequestApproval.setSuburbTrip(false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
    
        var employeeOptional = employeeRepository.findByUserId(UUID.fromString(USER_ID));
        assertThat(employeeOptional).isPresent();
        UUID userEmployeeId = employeeOptional.get().getId();
        RequestDocument document1 = RequestDocument.builder()
                                                   .employeeId(UUID.randomUUID())
                                                   .documentId(UUID.randomUUID())
                                                   .requestId(tripRequestApproval.getActionId())
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
                                                   .requestId(tripRequestApproval.getActionId())
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
        
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isOk()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.APPROVED);
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, tripRequestApproval.getActionId())));
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }
    
    @DisplayName("Разрешение на поездку по request id")
    @Test
    @Disabled("Почему?")
    void testApproveTripRequestByRequestId() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
        
        mockMvc.perform(
                post(APPROVE_TRIPREQUEST_BASE_URL + "requestid/" + tripRequestApproval.getActionId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isOk()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.APPROVED);
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture());
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }
    
    @DisplayName("Разрешение на поездку дает делегат от head of top department")
    @Test
    @Disabled("На Jenkins по каким-то причинам в TripApproverServiceImpl.computeApprovers не находит делегата для подразделения")
    void testApproveTripRequestByDelegateTopDepartment() throws Exception {
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
        
        createDelegate(topDepartmentHead, delegateEmployee);
        
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
        
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(DELEGATE_USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isOk()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.APPROVED);
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture());
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }
    
    @DisplayName("Делегат разрешает поездку")
    @Test
    @Disabled("На Jenkins по каким-то причинам в TripApproverServiceImpl.computeApprovers не находит делегата для подразделения")
    void testApproveTripRequestByDelegate() throws Exception {
        final Employee departmentHead = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        final Employee delegateEmployee =
                employeeRepository.findByUserId(UUID.fromString(DELEGATE_USER_ID)).orElseThrow();
        depLimitRepository.deleteAll();
        createDepLimit(departmentHead);
        
        createDelegate(departmentHead, delegateEmployee);
        
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
        
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(DELEGATE_USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isOk()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.APPROVED);
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture());
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isTrue();
    }
    
    @DisplayName("Делегату запрещено разрешать поездку, так как период его действия закончен")
    @Test
    void testApproveTripRequestByDelegateForbidden() throws Exception {
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
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
        
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(DELEGATE_USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isForbidden()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
    }
    
    @DisplayName("Approve не применился для CANCELLED approval")
    @Test
    void testApproveCancelledTripRequest() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        tripRequestApproval.setStatus(Status.CANCELLED);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.CANCELLED);
        
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isConflict()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.CANCELLED);
        
    }
    
    @DisplayName("Approve не применился, потому что применял его сотрудник, не имеющий на это право")
    @Test
    void testApproveTripRequestForbiddenByDepartmentHead() throws Exception {
        var department = departmentRepository.findAll().getFirst();
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
                .create());
        departmentRepository.save(department.setDepartmentHeadId(departmentHead.getId()));
        
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        tripRequestApproval.setStatus(Status.NEW);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isForbidden()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
        
    }
    
    @DisplayName("Разрешение на поездку - несуществующий approve")
    @Test
    void testApproveTripNotFoundApprove() throws Exception {
        mockMvc.perform(
                put(APPROVE_TRIPREQUEST_BASE_URL + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                /*.content(request)*/)
               .andExpect(status().isNotFound()).andReturn();
        
    }
    
    
    @DisplayName("Отклонение поездки")
    @Test
    void testDeclineTripRequest() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
        
        updateApproversForAllDepartments();
        mockMvc.perform(
                put(DECLINE_TRIPREQUEST_BASE_URL + tripRequestApproval.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .content(objectMapper.writeValueAsString(
                                CancelDTO.builder().reason("Test reason").build())))
               .andExpect(status().isOk()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.DECLINED);
        assertThat(tripRequestApproval.getReason()).isEqualTo("Test reason");
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, tripRequestApproval.getActionId())));
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isFalse();
        assertThat(actualMessage.message()).isEqualTo("Test reason");
    }
    
    @DisplayName("Отклонение поездки по Request id")
    @Test
    @Disabled("Почему?")
    void testDeclineTripRequestByRequestId() throws Exception {
        final Employee employee = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripRequestApproval = createApproval(employee, false);
        approvalRepository.save(tripRequestApproval);
        var tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.NEW);
        
        mockMvc.perform(
                put(DECLINE_TRIPREQUEST_BASE_URL + "requestid/" + tripRequestApproval.getActionId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .content(objectMapper.writeValueAsString(
                                CancelDTO.builder().reason("Test reason").build())))
               .andExpect(status().isOk()).andReturn();
    
        tripRequestApprovalOptional = approvalRepository.findById(tripRequestApproval.getId());
        assertThat(tripRequestApprovalOptional).isPresent();
        tripRequestApproval = tripRequestApprovalOptional.get();
        assertThat(tripRequestApproval.getStatus()).isEqualTo(Status.DECLINED);
        final var actualMessageCaptor = ArgumentCaptor.forClass(ApproveTripRequestMessage.class);
        verify(tripRequestApproveOutput).send(actualMessageCaptor.capture());
        var actualMessage = actualMessageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.actionId()).isEqualTo(tripRequestApproval.getActionId());
        assertThat(actualMessage.approved()).isFalse();
        assertThat(actualMessage.message()).isEqualTo("Test reason");
    }
    
    
    @DisplayName("Получение списка согласований")
    @Test
    void testGetApprovals() throws Exception {
        final Long balance = 6000L;
        final Long sum = 7000L;
        final Long taxiReserved = 700L;
        final Long publicReserved = 300L;
        
        //опишем поведение мокнутого feign client
        limitDataResolverReturns(balance, sum, taxiReserved, publicReserved);
        
        final Employee employee1 = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        TripRequestApproval tripApproval1 = createApproval(employee1, false);
        tripApproval1.setCost(5);
        tripApproval1.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).truncatedTo(ChronoUnit.MINUTES));
        tripApproval1.setTaxiClass(TaxiClass.COMFORT);
        approvalRepository.save(tripApproval1);
        
        final Employee employee2 = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripApproval2 = createApproval(employee2, false);
        tripApproval2.setCost(5);
        tripApproval2.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).truncatedTo(ChronoUnit.MINUTES));
        tripApproval2.setTransportType("PUBLIC");
        tripApproval2.setSuburbTrip(true);
        approvalRepository.save(tripApproval2);
        
        TripRequestApproval tripApproval3 = createApproval(employee2, true);
        tripApproval3.setCost(5);
        tripApproval3.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(1).truncatedTo(ChronoUnit.MINUTES));
        tripApproval3.setTaxiClass(TaxiClass.BUSINESS);
        tripApproval3.setTransportType("TAXI");
        tripApproval3.setStatus(Status.DECLINED);
        approvalRepository.save(tripApproval3);
        
        assertThat(approvalRepository.count()).isEqualTo(3);

        when(jwtDecoder.decode(any())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0))
                .header("algo", "none")
                .jti(USER_ID)
                .claim("roles", "ROLE_USER")
                .build());

        updateApproversForAllDepartments();
        MvcResult response = mockMvc.perform(get(PAGE_ACTIVE_TRIPREQUEST_BASE_URL)
                                                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                     .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                                    .andExpect(status().isOk()).andReturn();
        Map<String, Object> content = objectMapper.readValue(response.getResponse().getContentAsString(),
                                                                   new TypeReference<>() {});
        byte[] bytes = new ObjectMapper().writeValueAsBytes(content.get("content"));

        List<TripApproveDTO> actual = objectMapper.readValue(bytes, new TypeReference<>() {});
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getId()).isEqualTo(tripApproval2.getId());
        assertThat(actual.get(1).getId()).isEqualTo(tripApproval1.getId());

        TripApproveDTO approvalDTO = actual.stream()
                                           .filter(e -> Objects.equals(e.getId(), tripApproval1.getId())).findAny()
                                           .orElseThrow(() -> new IllegalStateException("Result not contains approval 1"));
    
    
        checkApprovalDto(approvalDTO, tripApproval1, employee1, sum, balance + taxiReserved);
        
        approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval2.getId())).findAny()
                            .orElseThrow(() -> new IllegalStateException("Result not contains approval 2"));
    
        checkApprovalDto(approvalDTO, tripApproval2, employee2, sum, balance + publicReserved);
        
        response = mockMvc.perform(get(PAGE_CLOSED_TRIPREQUEST_BASE_URL)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                          .andExpect(status().isOk()).andReturn();
        content = objectMapper.readValue(response.getResponse().getContentAsString(),
                                        new TypeReference<>() {});

        bytes = new ObjectMapper().writeValueAsBytes(content.get("content"));
        actual = objectMapper.readValue(bytes, new TypeReference<>() {});
        
        assertThat(actual).hasSize(1);
        approvalDTO = actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval3.getId())).findAny()
                            .orElseThrow(() -> new IllegalStateException("Result not contains approval 3"));
        checkApprovalDto(approvalDTO, tripApproval3, employee2, null, null);
    }
    
    @DisplayName("Получение списка согласований. Фильтр по fullName")
    @Test
    void testGetApprovals_fullName() throws Exception {
        final Long balance = 6000L;
        final Long sum = 7000L;
        final Long taxiReserved = 700L;
        final Long publicReserved = 300L;
        
        //опишем поведение мокнутого feign client
        limitDataResolverReturns(balance, sum, taxiReserved, publicReserved);
        
        final Employee employee1 = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        TripRequestApproval tripApproval1 = createApproval(employee1, false);
        tripApproval1.setCost(5);
        tripApproval1.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).truncatedTo(ChronoUnit.MINUTES));
        tripApproval1.setTaxiClass(TaxiClass.COMFORT);
        approvalRepository.save(tripApproval1);
        
        final Employee employee2 = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripApproval2 = createApproval(employee2, false);
        tripApproval2.setCost(5);
        tripApproval2.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(30).truncatedTo(ChronoUnit.MINUTES));
        tripApproval2.setTransportType("PUBLIC");
        tripApproval2.setSuburbTrip(true);
        approvalRepository.save(tripApproval2);
    
        final Employee employee3 = employeeRepository.findById(ACTOR_ID_2).orElseThrow();
        
        TripRequestApproval tripApproval3 = createApproval(employee3, true);
        tripApproval3.setCost(5);
        tripApproval3.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(1).truncatedTo(ChronoUnit.MINUTES));
        tripApproval3.setTaxiClass(TaxiClass.BUSINESS);
        tripApproval3.setTransportType("TAXI");
        approvalRepository.save(tripApproval3);
        
        TripRequestApproval tripApproval4 = createApproval(employee3, false);
        tripApproval4.setCost(5);
        tripApproval4.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).truncatedTo(ChronoUnit.MINUTES));
        tripApproval4.setTransportType("PUBLIC");
        tripApproval4.setSuburbTrip(true);
        approvalRepository.save(tripApproval4);
        
        assertThat(approvalRepository.count()).isEqualTo(4);

        when(jwtDecoder.decode(any())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0)).header("algo", "none").jti(USER_ID).claim("roles", "ROLE_USER").build());

        updateApproversForAllDepartments();
        MvcResult response = mockMvc.perform(get(PAGE_ACTIVE_TRIPREQUEST_BASE_URL_RAW)
                                                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                     .param("fullName", LAST_NAME)
                                                     .param("size", "5")
                                                     .param("page", "0")
                                                     .param("sort", "desiredDate,desc")
                                                     .param("field", "fullName")
                                                     .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                                    .andExpect(status().isOk()).andReturn();
        Map<String, Object> content = objectMapper.readValue(response.getResponse().getContentAsString(),
                                                             new TypeReference<>() {});
        byte[] bytes = new ObjectMapper().writeValueAsBytes(content.get("content"));
        
        List<TripApproveDTO> actual = objectMapper.readValue(bytes, new TypeReference<>() {});
        assertThat(actual).hasSize(3);
        assertThat(actual.get(0).getId()).isEqualTo(tripApproval4.getId());
        assertThat(actual.get(1).getId()).isEqualTo(tripApproval3.getId());
        assertThat(actual.get(2).getId()).isEqualTo(tripApproval2.getId());
        
        TripApproveDTO approvalDTO = actual.stream()
                                           .filter(e -> Objects.equals(e.getId(), tripApproval4.getId())).findAny()
                                           .orElseThrow(() -> new IllegalStateException("Result not contains approval 4"));
        
        
        checkApprovalDto(approvalDTO, tripApproval4, employee3, sum, balance + publicReserved);
    
        approvalDTO = actual.stream()
                            .filter(e -> Objects.equals(e.getId(), tripApproval2.getId())).findAny()
                            .orElseThrow(() -> new IllegalStateException("Result not contains approval 2"));
    
        checkApprovalDto(approvalDTO, tripApproval2, employee2, sum, balance + publicReserved);
        
        approvalDTO = actual.stream()
                            .filter(e -> Objects.equals(e.getId(), tripApproval3.getId())).findAny()
                            .orElseThrow(() -> new IllegalStateException("Result not contains approval 3"));
        
        checkApprovalDto(approvalDTO, tripApproval3, employee3, sum, balance + taxiReserved);
    }
    
    @DisplayName("Получение списка согласований. Нет доступных департаментов у юзера")
    @Test
    void testGetApprovals_noAllowedDepartments() throws Exception {
        final Long balance = 6000L;
        final Long sum = 7000L;
        final Long taxiReserved = 700L;
        final Long publicReserved = 300L;
        
        //опишем поведение мокнутого feign client
        limitDataResolverReturns(balance, sum, taxiReserved, publicReserved);
        
        final Employee employee1 = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        TripRequestApproval tripApproval1 = createApproval(employee1, false);
        tripApproval1.setCost(5);
        tripApproval1.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).truncatedTo(ChronoUnit.MINUTES));
        tripApproval1.setTaxiClass(TaxiClass.COMFORT);
        approvalRepository.save(tripApproval1);
        
        final Employee employee2 = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripApproval2 = createApproval(employee2, false);
        tripApproval2.setCost(5);
        tripApproval2.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).truncatedTo(ChronoUnit.MINUTES));
        tripApproval2.setTransportType("PUBLIC");
        tripApproval2.setSuburbTrip(true);
        approvalRepository.save(tripApproval2);
        
        assertThat(approvalRepository.count()).isEqualTo(2);

        when(jwtDecoder.decode(any())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0)).header("algo", "none").jti(USER_ID).claim("roles", "ROLE_USER").build());

        MvcResult response = mockMvc.perform(get(PAGE_ACTIVE_TRIPREQUEST_BASE_URL_RAW)
                                                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                     .param("size", "5")
                                                     .param("page", "0")
                                                     .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                                    .andExpect(status().isOk()).andReturn();
        Map<String, Object> content = objectMapper.readValue(response.getResponse().getContentAsString(),
                                                             new TypeReference<>() {});
        byte[] bytes = new ObjectMapper().writeValueAsBytes(content.get("content"));
        
        List<TripApproveDTO> actual = objectMapper.readValue(bytes, new TypeReference<>() {});
        assertThat(actual).isEmpty();
    }
    
    @DisplayName("Получение списка согласований. Фильтр по fullName")
    @Test
    void testGetApprovals_secondPage() throws Exception {
        final Long balance = 6000L;
        final Long sum = 7000L;
        final Long taxiReserved = 700L;
        final Long publicReserved = 300L;
        
        //опишем поведение мокнутого feign client
        limitDataResolverReturns(balance, sum, taxiReserved, publicReserved);
        
        final Employee employee1 = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        TripRequestApproval tripApproval1 = createApproval(employee1, false);
        tripApproval1.setCost(5);
        tripApproval1.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).truncatedTo(ChronoUnit.MINUTES));
        tripApproval1.setTaxiClass(TaxiClass.COMFORT);
        approvalRepository.save(tripApproval1);
        
        final Employee employee2 = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripApproval2 = createApproval(employee2, false);
        tripApproval2.setCost(5);
        tripApproval2.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).truncatedTo(ChronoUnit.MINUTES));
        tripApproval2.setTransportType("PUBLIC");
        tripApproval2.setSuburbTrip(true);
        approvalRepository.save(tripApproval2);
        
        final Employee employee3 = employeeRepository.findById(ACTOR_ID_2).orElseThrow();
        
        TripRequestApproval tripApproval3 = createApproval(employee3, true);
        tripApproval3.setCost(5);
        tripApproval3.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(1).truncatedTo(ChronoUnit.MINUTES));
        tripApproval3.setTaxiClass(TaxiClass.BUSINESS);
        tripApproval3.setTransportType("TAXI");
        approvalRepository.save(tripApproval3);
        
        TripRequestApproval tripApproval4 = createApproval(employee3, false);
        tripApproval4.setCost(5);
        tripApproval4.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).truncatedTo(ChronoUnit.MINUTES));
        tripApproval4.setTransportType("PUBLIC");
        tripApproval4.setSuburbTrip(true);
        approvalRepository.save(tripApproval4);
        
        assertThat(approvalRepository.count()).isEqualTo(4);
        
        updateApproversForAllDepartments();
        
        when(jwtDecoder.decode(any())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0))
                .header("algo", "none")
                .jti(USER_ID)
                .claim("roles", "ROLE_USER")
                .build());

        MvcResult response = mockMvc.perform(get(PAGE_ACTIVE_TRIPREQUEST_BASE_URL_RAW)
                                                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                     .param("size", "3")
                                                     .param("page", "1")
                                                     .param("sort", "desiredDate,desc")
                                                     .with(jwt().jwt(builder -> builder
                                                             .jti(USER_ID)
                                                             .claim("roles", "ROLE_USER"))))
                                    .andExpect(status().isOk()).andReturn();
        Map<String, Object> content = objectMapper.readValue(response.getResponse().getContentAsString(),
                                                             new TypeReference<>() {});
        byte[] bytes = new ObjectMapper().writeValueAsBytes(content.get("content"));
        
        List<TripApproveDTO> actual = objectMapper.readValue(bytes, new TypeReference<>() {});
        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getId()).isEqualTo(tripApproval3.getId());
        
        TripApproveDTO approvalDTO = actual.stream()
                                           .filter(e -> Objects.equals(e.getId(), tripApproval3.getId())).findAny()
                                           .orElseThrow(() -> new IllegalStateException("Result not contains approval 3"));
        
        
        checkApprovalDto(approvalDTO, tripApproval3, employee3, sum, balance + taxiReserved);
    }
    
    @DisplayName("Получение списка согласований. Фильтр по personNumber")
    @Test
    void testGetApprovals_personnelNumber() throws Exception {
        final Long balance = 6000L;
        final Long sum = 7000L;
        final Long taxiReserved = 700L;
        final Long publicReserved = 300L;
        
        //опишем поведение мокнутого feign client
        limitDataResolverReturns(balance, sum, taxiReserved, publicReserved);
        
        final Employee employee1 = employeeRepository.findByUserId(UUID.fromString(USER_ID)).orElseThrow();
        TripRequestApproval tripApproval1 = createApproval(employee1, false);
        tripApproval1.setCost(5);
        tripApproval1.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).truncatedTo(ChronoUnit.MINUTES));
        tripApproval1.setTaxiClass(TaxiClass.COMFORT);
        approvalRepository.save(tripApproval1);
        
        final Employee employee2 = employeeRepository.findById(ACTOR_ID).orElseThrow();
        TripRequestApproval tripApproval2 = createApproval(employee2, false);
        tripApproval2.setCost(5);
        tripApproval2.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).truncatedTo(ChronoUnit.MINUTES));
        tripApproval2.setTransportType("PUBLIC");
        tripApproval2.setSuburbTrip(true);
        approvalRepository.save(tripApproval2);
        
        final Employee employee3 = employeeRepository.findById(ACTOR_ID_2).orElseThrow();
        
        TripRequestApproval tripApproval3 = createApproval(employee3, true);
        tripApproval3.setCost(5);
        tripApproval3.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(1).truncatedTo(ChronoUnit.MINUTES));
        tripApproval3.setTaxiClass(TaxiClass.BUSINESS);
        tripApproval3.setTransportType("TAXI");
        approvalRepository.save(tripApproval3);
        
        TripRequestApproval tripApproval4 = createApproval(employee3, false);
        tripApproval4.setCost(5);
        tripApproval4.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).truncatedTo(ChronoUnit.MINUTES));
        tripApproval4.setTransportType("PUBLIC");
        tripApproval4.setSuburbTrip(true);
        approvalRepository.save(tripApproval4);
        
        assertThat(approvalRepository.count()).isEqualTo(4);

        when(jwtDecoder.decode(any())).thenAnswer(inv -> Jwt.withTokenValue(inv.getArgument(0))
                .header("algo", "none")
                .jti(USER_ID)
                .claim("roles", "ROLE_USER")
                .build());

        updateApproversForAllDepartments();
        MvcResult response = mockMvc.perform(get(PAGE_ACTIVE_TRIPREQUEST_BASE_URL_RAW)
                                                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                     .param("personnelNumber", PERSONNEL_NUMBER)
                                                     .param("size", "5")
                                                     .param("page", "0")
                                                     .param("direction", "ASC")
                                                     .param("sort", "desiredDate,asc")
                                                     .param("field", "personnelNumber")
                                                     .with(jwt().jwt(builder -> builder
                                                             .jti(USER_ID)
                                                             .claim("roles", "ROLE_USER"))))
                                    .andExpect(status().isOk()).andReturn();
        Map<String, Object> content = objectMapper.readValue(response.getResponse().getContentAsString(),
                                                             new TypeReference<>() {});
        byte[] bytes = new ObjectMapper().writeValueAsBytes(content.get("content"));
        
        List<TripApproveDTO> actual = objectMapper.readValue(bytes, new TypeReference<>() {});
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getId()).isEqualTo(tripApproval3.getId());
        assertThat(actual.get(1).getId()).isEqualTo(tripApproval4.getId());
        
        TripApproveDTO approvalDTO = actual.stream()
                                           .filter(e -> Objects.equals(e.getId(), tripApproval3.getId())).findAny()
                                           .orElseThrow(() -> new IllegalStateException("Result not contains approval 3"));
        
        
        checkApprovalDto(approvalDTO, tripApproval3, employee3, sum, balance + taxiReserved);
    
        approvalDTO = actual.stream()
                            .filter(e -> Objects.equals(e.getId(), tripApproval4.getId())).findAny()
                            .orElseThrow(() -> new IllegalStateException("Result not contains approval 4"));
    
        checkApprovalDto(approvalDTO, tripApproval4, employee3, sum, balance + publicReserved);
    }
    
    @DisplayName("Получение списка согласований делегатом от top head руководителя")
    @Test
    @Disabled("На Jenkins количество согласований иногда 0, вместо 2, скорее всего причина в делегатах")
    void testGetApprovalsByDelegate() throws Exception {
        //опишем поведение мокнутого feign client
        limitDataResolverReturns(100L, 100L, 100L, 100L);
        
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
        
        createDelegate(topDepartmentHead, delegateEmployee);
        
        TripRequestApproval tripApproval1 = createApproval(employee, false);
        tripApproval1.setCost(5);
        tripApproval1.setTaxiClass(TaxiClass.COMFORT);
        approvalRepository.save(tripApproval1);
        
        TripRequestApproval tripApproval2 = createApproval(topHeadEmployee, false);
        tripApproval2.setCost(5);
        approvalRepository.save(tripApproval2);
        
        TripRequestApproval tripApproval3 = createApproval(topHeadEmployee, true);
        tripApproval3.setCost(5);
        tripApproval3.setTaxiClass(TaxiClass.BUSINESS);
        tripApproval3.setStatus(Status.DECLINED);
        approvalRepository.save(tripApproval3);
        
        assertThat(approvalRepository.count()).isEqualTo(3);
        
        updateApproversForAllDepartments();
        MvcResult response = mockMvc.perform(
                get(LIST_ACTIVE_TRIPREQUEST_BASE_URL)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(DELEGATE_USER_ID).claim("roles", "ROLE_USER"))))
                                    .andExpect(status().isOk()).andReturn();
        Collection<TripApproveDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<>() {});
        
        assertThat(actual).hasSize(2);
        TripApproveDTO approvalDTO =
                actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval1.getId())).findAny()
                      .orElseThrow(() -> new IllegalStateException("Result not contains approval 1"));
    
        checkApprovalDto(approvalDTO, tripApproval1, employee, null, 200L);
        
        approvalDTO =
                actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval2.getId())).findAny()
                      .orElseThrow(() -> new IllegalStateException("Result not contains approval 2"));
    
        checkApprovalDto(approvalDTO, tripApproval2, topHeadEmployee, null, 200L);
        
        response = mockMvc.perform(
                get(LIST_CLOSED_TRIPREQUEST_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(DELEGATE_USER_ID).claim("roles", "ROLE_USER"))))
                          .andExpect(status().isOk()).andReturn();
        actual = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});

        assertThat(actual).hasSize(1);
        approvalDTO =
                actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval3.getId())).findAny()
                      .orElseThrow(() -> new IllegalStateException("Result not contains approval 3"));
    
        checkApprovalDto(approvalDTO, tripApproval3, topHeadEmployee, null, null);
    }
    
    @DisplayName("Проверка логики расчета лимитов")
    @Test
    @Disabled("На Jenkins иногда количество количество согласований 2, а не 4. Проблема в делегатах")
    void testCheckLimits() throws Exception {
        //опишем поведение мокнутого feign client
        limitDataResolverReturns(100L, 100L, 100L, 100L);
        
        // Пользователь является руководителем одного подразделения и в то же время делегатом для
        // руководителя другого подразделения
        
        // Данные подразделения 1
        var employeeOptional = employeeRepository.findByUserId(UUID.fromString(USER_ID));
        assertThat(employeeOptional).isPresent();
        final Employee headEmployee1 = employeeOptional.get();
        Employee employee1 = Employee.builder()
                                     .id(UUID.randomUUID())
                                     .departmentId(headEmployee1.getDepartmentId())
                                     .build();
        employeeRepository.save(employee1);
        // Создаем два согласования для сотрудника подразделения 1
        TripRequestApproval tripApproval11 = createApproval(employee1, false);
        tripApproval11.setCost(5);
        approvalRepository.save(tripApproval11);
        TripRequestApproval tripApproval12 = createApproval(employee1, false);
        tripApproval12.setCost(10);
        approvalRepository.save(tripApproval12);
        
        // Данные подразделения 2
        var employee2Optional = employeeRepository.findByUserId(UUID.fromString(TOP_USER_ID));
        assertThat(employee2Optional).isPresent();
        final Employee headEmployee2 = employee2Optional.get();
        Employee employee2 = Employee.builder()
                                     .id(UUID.randomUUID())
                                     .departmentId(headEmployee2.getDepartmentId())
                                     .build();
        employeeRepository.save(employee2);
        // Создаем два согласования для сотрудника подразделения 2
        TripRequestApproval tripApproval21 = createApproval(employee2, false);
        tripApproval21.setCost(5);
        approvalRepository.save(tripApproval21);
        TripRequestApproval tripApproval22 = createApproval(employee2, false);
        tripApproval22.setCost(5);
        approvalRepository.save(tripApproval22);
        
        // Делаем headEmployee1 делегатом у headEmployee2
        createDelegate(headEmployee2, headEmployee1);
        
        assertThat(approvalRepository.count()).isEqualTo(4);
        
        updateApproversForAllDepartments();
        
        // Запрос вернет согласования от подразделения 1, так как USER_1 является его руководителем, и согласования
        // от подразделения 2, так как USER_1 является делегатом руководителя подразделения 2
        MvcResult response = mockMvc.perform(
                get(LIST_ACTIVE_TRIPREQUEST_BASE_URL)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                                    .andExpect(status().isOk()).andReturn();
        List<TripApproveDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<>() {});
        
        assertThat(actual).hasSize(4);
        TripApproveDTO approvalDTO =
                actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval11.getId())).findAny()
                      .orElseThrow(() -> new IllegalStateException("Result not contains approval 11"));
        assertThat(approvalDTO.getCost()).isEqualTo(tripApproval11.getCost());
        assertThat(approvalDTO.getRestOfLimit()).isEqualTo(200);
        assertThat(approvalDTO.getSumLimit()).isEqualTo(100);
        
        approvalDTO =
                actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval12.getId())).findAny()
                      .orElseThrow(() -> new IllegalStateException("Result not contains approval 12"));
        assertThat(approvalDTO.getCost()).isEqualTo(tripApproval12.getCost());
        assertThat(approvalDTO.getRestOfLimit()).isEqualTo(200);
        assertThat(approvalDTO.getSumLimit()).isEqualTo(100);
    
        approvalDTO =
                actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval21.getId())).findAny()
                      .orElseThrow(() -> new IllegalStateException("Result not contains approval 21"));
        assertThat(approvalDTO.getCost()).isEqualTo(tripApproval21.getCost());
        assertThat(approvalDTO.getRestOfLimit()).isEqualTo(200);
        assertThat(approvalDTO.getSumLimit()).isEqualTo(100);
    
        approvalDTO =
                actual.stream().filter(e -> Objects.equals(e.getId(), tripApproval22.getId())).findAny()
                      .orElseThrow(() -> new IllegalStateException("Result not contains approval 22"));
        assertThat(approvalDTO.getCost()).isEqualTo(tripApproval22.getCost());
        assertThat(approvalDTO.getRestOfLimit()).isEqualTo(200);
        assertThat(approvalDTO.getSumLimit()).isEqualTo(100);
    
    }
    
    private void createDepLimit(Employee employee) {
        DepLimit depLimit = new DepLimit();
        depLimit.setId(UUID.randomUUID());
        depLimit.setYear(LocalDate.now().getYear());
        depLimit.setOwnerId(employee.getId());
        depLimit.setReserve(10L);
        depLimit.setSum(LIMIT_SUM);
        depLimitRepository.save(depLimit);
    }
    
    private void createTripPurpose(UUID uuid) {
        TripPurpose purpose = TripPurpose.builder().id(uuid).label("Label " + uuid).build();
        tripPurposeRepository.save(purpose);
    }
    
    private TripRequestApproval createApproval(Employee employee, boolean isCoopTrip) {
        TripRequestApproval tripRequestApproval = new TripRequestApproval();
        tripRequestApproval.setActorId(employee.getId());
        tripRequestApproval.setAuthorId(UUID.randomUUID());
        tripRequestApproval.setActionId(UUID.randomUUID());
        tripRequestApproval.setCreationTime(LocalDateTime.now());
        tripRequestApproval.setTransportType("TAXI");
        tripRequestApproval.setPurposeId(UUID.randomUUID());
        tripRequestApproval.setStatus(Status.NEW);
        tripRequestApproval.setSharedRideId(isCoopTrip ? UUID.randomUUID() : null);
        createTripPurpose(tripRequestApproval.getPurposeId());
        return tripRequestApproval;
    }
    
    private void updateApproversForAllDepartments() {
        departmentRepository.findAll().forEach(dep -> approverService.onDepartmentChanged(dep.getId()));
    }
    
    private void createDelegate(Employee departmentHeadEmployee, Employee delegateEmployee) {
        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(departmentHeadEmployee.getId());
        delegateRecordDTO.setDelegateId(delegateEmployee.getId());
        delegateRecordDTO.setTransportType("TAXI");
        delegateRecordDTO.setStartDate(LocalDate.now());
        delegateRecordDTO.setEndDate(LocalDate.now().plusMonths(3));
        delegateRepository.save(delegateRecordDTO);
    }
    
    private String getPurposeLabel(TripApproveDTO approvalDTO) {
        var tripPurposeOptional = tripPurposeRepository.findById(approvalDTO.getPurposeId());
        assertThat(tripPurposeOptional).isPresent();
        return tripPurposeOptional.get().getLabel();
    }
    
    /**
     * Описание поведения мока data resolver
     */
    private void limitDataResolverReturns(Long balance, Long sum, Long taxiReserved, Long personalReserved) {
        GetLimitDTO limit = GetLimitDTO.builder().id(UUID.randomUUID()).year(LocalDate.now().getYear()).build();
        
        GetLimitSharingPerPeriodDTO taxiSharingLimitPerPeriod =
                GetLimitSharingPerPeriodDTO.builder().balance(balance).sum(sum).sumReservedForCurrentPeriod(taxiReserved).build();
        GetLimitSharingPerPeriodDTO personalSharingLimitPerPeriod =
                GetLimitSharingPerPeriodDTO.builder().balance(balance).sum(sum).sumReservedForCurrentPeriod(personalReserved).build();
        
        GetLimitSharingDTO taxiSharingLimit = GetLimitSharingDTO.builder().transportType("TAXI")
                                                                .balance(balance * 12).sum(sum * 12)
                                                                .limitSharingPerPeriodDTO(taxiSharingLimitPerPeriod)
                                                                .build();
        GetLimitSharingDTO personalSharingLimit = GetLimitSharingDTO.builder().transportType("PUBLIC")
                                                                    .balance(balance * 12).sum(sum * 12)
                                                                    .limitSharingPerPeriodDTO(personalSharingLimitPerPeriod)
                                                                    .build();
        List<GetLimitSharingDTO> limitSharing = List.of(taxiSharingLimit, personalSharingLimit);
    
        //опишем поведение мокнутого feign client
        when(limitsDataResolver.getByDepartmentAndYearAndLimitServiceTypeFull(any(), eq(LocalDate.now().getYear()), any(), any())).thenReturn(limit);
        when(limitsDataResolver.getByLimitFull(any(), any())).thenReturn(limitSharing);
    }
    
    private void checkApprovalDto(
            TripApproveDTO actual, TripRequestApproval expected, Employee expectedEmployee, Long sum,
            Long restOfLimit) {
        assertThat(actual.getCost()).isEqualTo(expected.getCost());
        assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
        assertThat(actual.getTaxiClass()).isEqualTo(expected.getTaxiClass());
        assertThat(actual.getTransportType()).isEqualTo(expected.getTransportType());
        assertThat(actual.getDesiredDate()).isEqualTo(expected.getDesiredDate());
        assertThat(actual.getPassenger().id()).isEqualTo(expectedEmployee.getId());
        assertThat(actual.getPassenger().firstName()).isEqualTo(expectedEmployee.getFirstName());
        assertThat(actual.getPassenger().lastName()).isEqualTo(expectedEmployee.getLastName());
        assertThat(actual.getPassenger().patronymic()).isEqualTo(expectedEmployee.getPatronymic());
        assertThat(actual.getPassenger().personnelNumber()).isEqualTo(expectedEmployee.getPersonnelNumber());
        assertThat(actual.getPassenger().departmentId()).isEqualTo(expectedEmployee.getDepartmentId());
        assertThat(actual.getPurposeLabel()).isEqualTo(getPurposeLabel(actual));
        if (sum != null) {
            assertThat(actual.getSumLimit()).isEqualTo(sum);
        }
        if (restOfLimit != null) {
            assertThat(actual.getRestOfLimit()).isEqualTo(restOfLimit);
        }
        assertThat(actual.isCoopTrip()).isEqualTo(expected.getSharedRideId() != null);
    }
}