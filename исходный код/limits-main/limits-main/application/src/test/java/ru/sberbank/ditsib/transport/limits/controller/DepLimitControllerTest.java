package ru.sberbank.ditsib.transport.limits.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.CommonTest;
import ru.sberbank.ditsib.transport.limits.LimitsApplication;
import ru.sberbank.ditsib.transport.limits.config.HibernateEventsConfiguration;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.constants.RemainsTransferTarget;
import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.model.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.database.limits.Tables.SERVICES;
import static ru.sber.transport.database.limits.Tables.TYPES;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest(classes = LimitsApplication.class)
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера лимитов подразделений")
@MockitoBean(types = {
        EmployeesGrpc.EmployeesBlockingStub.class,
        DepartmentsGrpc.DepartmentsBlockingStub.class
})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class DepLimitControllerTest extends CommonTest {
    @Autowired
    private DSLContext dslContext;
    @MockitoBean
    private HibernateEventsConfiguration hibernateEventsConfiguration;
    @Autowired
    private DepLimitService depLimitService;
    @Autowired
    private DepLimitRepository depLimitRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private LimitSharingService limitSharingService;
    @Autowired
    private LimitSharingPerPeriodService<Month> limitSharingPerPeriodService;
    @Autowired
    AutoCloseLimitService autoCloseLimitService;
    @Autowired
    LimitSettingsService limitSettingsService;
    @Autowired
    LimitSharingController limitSharingController;
    @Autowired
    LimitStatsService limitStatsService;
    @Autowired
    LimitHistoryRepository limitHistoryRepository;
    @MockitoBean
    LimitStatsRefreshService limitStatsRefreshService;
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @BeforeEach
    public void init() {
        AuthorizeUtils.authorize(authorizationManager, "ROLE_GUEST");
        dslContext.insertInto(SERVICES)
                .values("PASSENGER")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("PUBLIC")
                .onConflictDoNothing().execute();
        dslContext.insertInto(SERVICES)
                .values("CARGO")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("TAXI")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("DEDICATED")
                .onConflictDoNothing().execute();
        organization1 = organizationRepository.save(Organization.builder().id(organizationId).digitId(1L).build());
        departmentMain = departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .organizationId(organization1.getId())
                .code("1111")
                .active(true)
                .departmentName("moscow")
                .build());
        departmentChild1 = departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .organizationId(organization1.getId())
                .code("2222")
                .active(true)
                .departmentName("voronej")
                .parentId(departmentMain.getId())
                .build());
        departmentChild2 = departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .organizationId(organization1.getId())
                .code("3333")
                .active(true)
                .departmentName("kursk")
                .parentId(departmentMain.getId())
                .build());
        departmentChild1OfChild1 = departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .organizationId(organization1.getId())
                .code("4444")
                .departmentName("sahalin")
                .parentId(departmentChild1.getId())
                .build());

        testEmployee1 = employeeRepository.save(Employee.builder()
                .id(userId1)
                .userId(userId1)
                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1)
                .firstName("vasya")
                .lastName("pupkin")
                .organizationId(organization1.getId())
                .departmentId(departmentMain.getId())
                .build());

        testEmployee2 = employeeRepository.save(Employee.builder()
                .id(userId2)
                .userId(userId2)
                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2)
                .firstName("masha")
                .lastName("sidorova")
                .organizationId(organization1.getId())
                .departmentId(departmentMain.getId())
                .build());

        testEmployee1.setSupervisorId(testEmployee2.getId());
        employeeRepository.save(testEmployee1);

        departmentMain.setDepartmentHead(testEmployee1);
        departmentRepository.save(departmentMain);
        departmentChild1.setDepartmentHead(testEmployee1);
        departmentRepository.save(departmentChild1);
        departmentChild2.setDepartmentHead(testEmployee1);
        departmentRepository.save(departmentChild2);
        departmentChild1OfChild1.setDepartmentHead(testEmployee1);
        departmentRepository.save(departmentChild1OfChild1);
        limitHistoryRepository.deleteAll();
    }

    @AfterEach
    void afterEach() {
        limitSpendingRepository.deleteAll();
        limitSharingPerPeriodRepository.deleteAll();
        limitSharingRepository.deleteAll();
        depLimitRepository.deleteAll();
        empLimitRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
        limitHistoryRepository.deleteAll();
    }

    @Test
    @DisplayName("CRUD лимитов подразделений")
    void test_CRUD() throws Exception {
        List<DepLimit> depLimitList1 = depLimitRepository.findAll();
        assertEquals(0, depLimitList1.size());

        var getLimitDTO = postAndGetLimit(2021, BigDecimal.valueOf(100), false, "PASSENGER");

        List<DepLimit> depLimitList2 = depLimitRepository.findAll();
        assertEquals(1, depLimitList2.size());

        DepLimitPrimaryDTO depLimitPrimaryDTO2 = new DepLimitPrimaryDTO();
        depLimitPrimaryDTO2.setYear(getLimitDTO.getYear());
        depLimitPrimaryDTO2.setLimitSharingType(getLimitDTO.getLimitSharingType());
        depLimitPrimaryDTO2.setLimitServiceType(getLimitDTO.getLimitServiceType());
        depLimitPrimaryDTO2.setFinalSharing(getLimitDTO.getFinalSharing());
        depLimitPrimaryDTO2.setUseThisLimit(getLimitDTO.getUseThisLimit());
        depLimitPrimaryDTO2.setSum(BigDecimal.valueOf(200));
        String depLimitStr2 = objectMapper.writeValueAsString(depLimitPrimaryDTO2);

        String url = "/deplimits/" + getLimitDTO.getId();

        //perform edit
        mockMvc.perform(put(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(depLimitStr2))
                .andExpect(status().isOk());

        //perform get
        mockMvc.perform(get(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(depLimitPrimaryDTO2.getSum().multiply(BigDecimal.valueOf(100)).longValue()))
                .andExpect(jsonPath("$.id").value(getLimitDTO.getId().toString()));

        //perform delete
        depLimitService.delete(depLimitService.get(getLimitDTO.getId()).orElse(null));

        List<DepLimit> depLimitList3 = depLimitRepository.findAll();
        assertEquals(0, depLimitList3.size());

        List<LimitHistory> historyList = limitHistoryRepository.findAll();
        assertEquals(2, historyList.size());
        List<LimitHistory> historyListDepartmentMain = limitHistoryRepository.findByLimitIdOrderByCreationTime(getLimitDTO.getId());
        assertEquals(2, historyListDepartmentMain.size());
    }

    @Test
    @DisplayName("Получение всех лимитов подразделений")
    void test_getAll() throws Exception {
        var response = mockMvc.perform(
                        get("/deplimits")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        List<GetLimitDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(depLimitRepository.findAll().size(), actual.size());
    }

    @Test
    @DisplayName("Перераспределение лимита между подразделениями")
    @Disabled("Требуется переработка")
    void test_reSharePrimaryLimitBetweenDepartments() throws Exception { // NOSONAR
        // предусловие: подразделение, у него 2 дочерних, у них сотрудники
        // создать лимит организации первичный
        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR, 1);
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();

        List<DepLimit> depLimitList1 = depLimitRepository.findAll();
        assertEquals(0, depLimitList1.size());

        var getLimitDTO = postAndGetLimit(plannedYear, BigDecimal.valueOf(120), false, "PASSENGER");

        UUID parentLimitId = getLimitDTO.getId();

        // распределить на такси
        postShareLimit(TransportTypeEnum.TAXI, BigDecimal.valueOf(120), parentLimitId);

        Limit limitMain = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(limitMain).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitMain.getLimitStatus());
        assertEquals(120, limitMain.getSum().intValue());
        List<LimitSharing> limitSharingList = limitSharingService.getByLimit(limitMain);
        assertEquals(1, limitSharingList.size());
        assertEquals(120, limitSharingList.getFirst().getSum().intValue());

        var imitList = limitSharingService.distributeSharingPerPeriodImitation(limitMain, BigDecimal.valueOf(40));
        assertEquals(12, imitList.size());

        // share secondary
        List<DepLimitSharingPerTransportDTO> depLimitSharingPerTransportDTOList1 = new ArrayList<>();
        DepLimitSharingPerTransportDTO depLimitSharingPerTransportDTO1 =
                DepLimitSharingPerTransportDTO.builder()
                        .transportType(TransportTypeEnum.TAXI).sum(BigDecimal.valueOf(40)).build();
        depLimitSharingPerTransportDTOList1.add(depLimitSharingPerTransportDTO1);
        DepLimitSharingDTO secDto1 = DepLimitSharingDTO.builder()
                .sharingPerTransportList(depLimitSharingPerTransportDTOList1)
                .targetDepartmentId(departmentChild1.getId())
                .finalSharing(true)
                .build();
        List<DepLimitSharingPerTransportDTO> depLimitSharingPerTransportDTOList2 = new ArrayList<>();
        DepLimitSharingPerTransportDTO depLimitSharingPerTransportDTO2 =
                DepLimitSharingPerTransportDTO.builder()
                        .transportType(TransportTypeEnum.TAXI).sum(BigDecimal.valueOf(40)).build();
        depLimitSharingPerTransportDTOList2.add(depLimitSharingPerTransportDTO2);
        DepLimitSharingDTO secDto2 = DepLimitSharingDTO.builder()
                .sharingPerTransportList(depLimitSharingPerTransportDTOList2)
                .targetDepartmentId(departmentChild2.getId())
                .finalSharing(false)
                .build();
        List<DepLimitSharingDTO> limitSharingDTOList12 = new ArrayList<>();
        limitSharingDTOList12.add(secDto1);
        limitSharingDTOList12.add(secDto2);
        String limitSharingStr12 = objectMapper.writeValueAsString(limitSharingDTOList12);
        mockMvc.perform(post("/deplimits/share_secondary/" + limitMain.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr12))
                .andExpect(status().isOk());

        limitMain = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(limitMain).isNotNull();
        assertEquals(LimitStatus.SHARED, limitMain.getLimitStatus());
        assertEquals(120, limitMain.getSum().intValue());
        limitSharingList = limitSharingService.getByLimit(limitMain);
        assertEquals(1, limitSharingList.size());
        assertEquals(40, limitSharingList.getFirst().getSum().intValue());
        var limitSharingPerPeriodList =
                limitSharingPerPeriodService.getByLimitSharing(limitSharingList.getFirst());
        assertEquals(12, limitSharingPerPeriodList.size());

        Limit limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        Limit limitChild2 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild2.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild2).isNotNull();

        List<DepLimitSharingDTO> limitSharingDTOListEmpty = new ArrayList<>();
        String limitSharingStrEmpty = objectMapper.writeValueAsString(limitSharingDTOListEmpty);

        mockMvc.perform(post("/deplimits/share_secondary/" + limitChild1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStrEmpty))
                .andExpect(status().isOk());
        mockMvc.perform(post("/deplimits/share_secondary/" + limitChild2.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStrEmpty))
                .andExpect(status().isOk());

        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(40, limitChild1.getSum().intValue());
        List<LimitSharing> limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(40, limitSharingList1.getFirst().getSum().intValue());
        var limitSharingPerPeriodList1 =
                limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());
        assertEquals(LimitStatus.SHARED, limitChild2.getLimitStatus());
        assertEquals(40, limitChild2.getSum().intValue());
        List<LimitSharing> limitSharingList2 = limitSharingService.getByLimit(limitChild2);
        assertEquals(1, limitSharingList2.size());
        assertEquals(40, limitSharingList2.getFirst().getSum().intValue());
        var limitSharingPerPeriodList2 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList2.getFirst());
        assertEquals(12, limitSharingPerPeriodList2.size());

        var limitList = limitStatsService.getByOrganizationIdAndYearAndPeriodAndTransportType(List.of(organizationId),
                plannedYear, Arrays.asList(Month.values()), List.of(TransportTypeEnum.TAXI));
        assertEquals(12, limitList.size());

        // перераспределеить можду дочерними подразделениями
        LimitReSharingByDepartmentDTO limitReSharingByDepartmentDTO = new LimitReSharingByDepartmentDTO();
        limitReSharingByDepartmentDTO.setYear(plannedYear);
        limitReSharingByDepartmentDTO.setSourceDepartmentId(departmentChild1.getId());
        limitReSharingByDepartmentDTO.setSourceTransportType(TransportTypeEnum.TAXI);
        limitReSharingByDepartmentDTO.setTargetDepartmentId(departmentChild2.getId());
        limitReSharingByDepartmentDTO.setTargetTransportType(TransportTypeEnum.TAXI);
        limitReSharingByDepartmentDTO.setSum(BigDecimal.valueOf(20));

        String limitReSharingByDepartmentDTOStr = objectMapper.writeValueAsString(limitReSharingByDepartmentDTO);

        mockMvc.perform(post("/deplimits/reshare_departments")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitReSharingByDepartmentDTOStr))
                .andExpect(status().isOk());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(20, limitChild1.getSum().intValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(20, limitSharingList1.getFirst().getSum().intValue());

        limitChild2 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild2.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild2).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild2.getLimitStatus());
        assertEquals(60, limitChild2.getSum().intValue());
        limitSharingList2 = limitSharingService.getByLimit(limitChild2);
        assertEquals(1, limitSharingList2.size());
        assertEquals(60, limitSharingList2.getFirst().getSum().intValue());

        List<String> auditList = depLimitService.auditLimits(organizationId, plannedYear, true);
        assertThat(auditList).isNotEmpty();
        assertThat(auditList.stream().filter(e -> e.indexOf("ERROR") > 0).count()).isZero();

        List<LimitHistory> historyList = limitHistoryRepository.findAll();
        assertEquals(9, historyList.size());
        List<LimitHistory> historyListDepartmentMain = limitHistoryRepository.findByLimitIdOrderByCreationTime(limitMain.getId());
        assertEquals(3, historyListDepartmentMain.size());
        List<LimitHistory> historyListLimitChild1 = limitHistoryRepository.findByLimitIdOrderByCreationTime(limitChild1.getId());
        assertEquals(3, historyListLimitChild1.size());
        List<LimitHistory> historyListLimitChild2 = limitHistoryRepository.findByLimitIdOrderByCreationTime(limitChild2.getId());
        assertEquals(3, historyListLimitChild2.size());
    }

    @Test
    @DisplayName("Перераспределение лимита между видами транспорта")
    void test_reShareLimitBetweenTransportTypes() throws Exception {
        // предусловие: подразделение, у него 2 дочерних, у них сотрудники
        // создать лимит организации первичный

        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR, 1);
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();

        List<DepLimit> depLimitList1 = depLimitRepository.findAll();
        assertEquals(0, depLimitList1.size());

        var getLimitDTO = postAndGetLimit(plannedYear, BigDecimal.valueOf(80), false, "PASSENGER");

        UUID parentLimitId = getLimitDTO.getId();

        // распределить себе и на дочернее подразделение на такси
        postShareLimit(TransportTypeEnum.TAXI, BigDecimal.valueOf(40), parentLimitId);
        postShareLimit(TransportTypeEnum.PUBLIC, BigDecimal.valueOf(40), parentLimitId);

        Limit limit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(limit).isNotNull();
        assertEquals(LimitStatus.PLANNING, limit.getLimitStatus());
        assertEquals(80, limit.getSum().intValue());
        List<LimitSharing> limitSharingList = limitSharingService.getByLimit(limit);
        assertEquals(2, limitSharingList.size());
        assertEquals(40, limitSharingList.getFirst().getSum().intValue());
        assertEquals(40, limitSharingList.get(1).getSum().intValue());

        // перераспределеить между видами транспорта
        LimitReSharingByTransportTypeDTO limitReSharingByTransportTypeDTO = new LimitReSharingByTransportTypeDTO();
        limitReSharingByTransportTypeDTO.setLimitId(limit.getId());
        limitReSharingByTransportTypeDTO.setSourceTransportType(TransportTypeEnum.TAXI);
        limitReSharingByTransportTypeDTO.setTargetTransportType(TransportTypeEnum.PUBLIC);
        limitReSharingByTransportTypeDTO.setSum(BigDecimal.valueOf(20));

        String limitReSharingByTransportTypeDTOStr = objectMapper.writeValueAsString(limitReSharingByTransportTypeDTO);

        mockMvc.perform(post("/deplimits/reshare_transporttypes")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitReSharingByTransportTypeDTOStr))
                .andExpect(status().isOk());
        limit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(limit).isNotNull();
        assertEquals(LimitStatus.PLANNING, limit.getLimitStatus());
        assertEquals(80, limit.getSum().intValue());
        limitSharingList = limitSharingService.getByLimit(limit);
        assertEquals(2, limitSharingList.size());
        assertEquals(20, limitSharingList.get(1).getSum().intValue());
        assertEquals(60, limitSharingList.get(0).getSum().intValue());

        List<String> auditList = depLimitService.auditLimits(organizationId, plannedYear, true);
        assertThat(auditList).isNotEmpty();
        assertThat(auditList.stream().filter(e -> e.indexOf("ERROR") > 0).count()).isZero();
    }

    @Test
    @DisplayName("Забор остатка лимита от дочернего подразделения к главному")
    void test_reShareLimitFromMainAndTakeBack() throws Exception { // NOSONAR
        // предусловие: подразделение, у него 2 дочерних, у них сотрудники
        // создать лимит организации первичный

        var now = LocalDate.now(ZoneOffset.UTC);
        var next = now.plusYears(1);
        int plannedYear = next.getYear();

        List<DepLimit> depLimitList1 = depLimitRepository.findAll();
        assertEquals(0, depLimitList1.size());

        var getLimitDTO = postAndGetLimit(plannedYear, BigDecimal.valueOf(80), false, "PASSENGER");

        UUID parentLimitId = getLimitDTO.getId();

        // распределить резерв на такси
        postShareLimit(TransportTypeEnum.TAXI, BigDecimal.valueOf(80), parentLimitId);

        DepLimit limitMain = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(limitMain).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitMain.getLimitStatus());
        assertEquals(80, limitMain.getSum().longValue());
        assertEquals(0, limitMain.getReserve().longValue());
        List<LimitSharing> limitSharingList = limitSharingService.getByLimit(limitMain);
        assertEquals(1, limitSharingList.size());
        assertEquals(80, limitSharingList.getFirst().getSum().longValue());

        // выделить дочернему подразделению
        List<DepLimitSharingPerTransportDTO> depLimitSharingPerTransportDTOList = new ArrayList<>();
        DepLimitSharingPerTransportDTO depLimitSharingPerTransportDTO =
                DepLimitSharingPerTransportDTO.builder()
                        .transportType(TransportTypeEnum.TAXI).sum(BigDecimal.valueOf(40)).build();
        depLimitSharingPerTransportDTOList.add(depLimitSharingPerTransportDTO);
        DepLimitSharingDTO secDto1 = DepLimitSharingDTO.builder()
                .sharingPerTransportList(depLimitSharingPerTransportDTOList)
                .targetDepartmentId(departmentChild1.getId())
                .finalSharing(false)
                .build();
        List<DepLimitSharingDTO> limitSharingDTOList12 = new ArrayList<>();
        limitSharingDTOList12.add(secDto1);

        String limitSharingStr12 = objectMapper.writeValueAsString(limitSharingDTOList12);
        mockMvc.perform(post("/deplimits/share_secondary/" + limitMain.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr12))
                .andExpect(status().isOk());

        DepLimit mainLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(mainLimit).isNotNull();
        assertEquals(LimitStatus.SHARED, mainLimit.getLimitStatus());
        assertEquals(80, mainLimit.getSum().intValue());
        assertEquals(0, mainLimit.getReserve().intValue());
        limitSharingList = limitSharingService.getByLimit(mainLimit);
        assertEquals(1, limitSharingList.size());
        assertEquals(40, limitSharingList.getFirst().getSum().intValue());
        var limitSharingPerPeriodList = limitSharingPerPeriodService.getByLimitSharing(limitSharingList.getFirst());
        assertEquals(12, limitSharingPerPeriodList.size());

        Limit limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitChild1.getLimitStatus());
        assertEquals(40, limitChild1.getSum().intValue());
        limitSharingList = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList.size());
        assertEquals(40, limitSharingList.getFirst().getSum().intValue());
        var limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList.getFirst());
        assertEquals(0, limitSharingPerPeriodList1.size());

        // распределить дочернему подразделению
        String limitSharingStr13 = objectMapper.writeValueAsString(new ArrayList<>());
        mockMvc.perform(post("/deplimits/share_secondary/" + limitChild1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr13))
                .andExpect(status().isOk());


        limitSharingController.getByLimitFull(limitChild1.getId());

        // забрать часть остатка у дочернего подразделения назад
        LimitResharingDTO limitResharingDTO = new LimitResharingDTO();
        limitResharingDTO.setSourceLimitId(limitChild1.getId());
        limitResharingDTO.setSourceTransportType(TransportTypeEnum.TAXI);
        limitResharingDTO.setTargetLimitId(mainLimit.getId());
        limitResharingDTO.setTargetTransportType(TransportTypeEnum.TAXI);
        limitResharingDTO.setSum(BigDecimal.valueOf(20));

        String limitReSharingTakeFromChildDepartmentDTOStr =
                objectMapper.writeValueAsString(limitResharingDTO);

        mockMvc.perform(post("/deplimits/reshare_transfer")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                limitReSharingTakeFromChildDepartmentDTOStr))
                .andExpect(status().isOk());

        mainLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(mainLimit).isNotNull();
        assertEquals(LimitStatus.SHARED, mainLimit.getLimitStatus());
        assertEquals(80, mainLimit.getSum().intValue());
        assertEquals(0, mainLimit.getReserve().intValue());
        limitSharingList = limitSharingService.getByLimit(mainLimit);
        assertEquals(1, limitSharingList.size());
        assertEquals(60, limitSharingList.getFirst().getSum().intValue());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(20, limitChild1.getSum().intValue());
        limitSharingList = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList.size());
        assertEquals(20, limitSharingList.getFirst().getSum().intValue());
        limitSharingPerPeriodList = limitSharingPerPeriodService.getByLimitSharing(limitSharingList.getFirst());
        assertEquals(12, limitSharingPerPeriodList.size());

        limitSettingsService.add(SettingsNames.DEP_LIMIT_REMAINS_TARGET, RemainsTransferTarget.TO_RESERVE.name());
        autoCloseLimitService.returnRemains(Month.DECEMBER);

        mainLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(mainLimit).isNotNull();
        assertEquals(LimitStatus.SHARED, mainLimit.getLimitStatus());
        assertEquals(80, mainLimit.getSum().intValue());
        assertEquals(0, mainLimit.getReserve().intValue());
        assertEquals(9, mainLimit.getEconomy().intValue());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(11, limitChild1.getSum().intValue());
        limitSharingList = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList.size());
        assertEquals(11, limitSharingList.getFirst().getSum().intValue());

        // выделить из экономии
        DepLimitEconomyDTO economyDTO = DepLimitEconomyDTO.builder()
                .transportType(TransportTypeEnum.PUBLIC)
                .sum(BigDecimal.valueOf(9))
                .targetDepartmentId(departmentChild1.getId())
                .build();
        List<DepLimitEconomyDTO> limitEconomyDTOList = new ArrayList<>();
        limitEconomyDTOList.add(economyDTO);
        String economyStr = objectMapper.writeValueAsString(limitEconomyDTOList);
        mockMvc.perform(post("/deplimits/share_economy/" + limitMain.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(economyStr))
                .andExpect(status().isOk());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(20, limitChild1.getSum().intValue());
        limitSharingList = limitSharingService.getByLimit(limitChild1);
        assertEquals(2, limitSharingList.size());
        LimitSharing limitSharing1 = limitSharingList.get(1);
        assertEquals(11, limitSharing1.getSum().intValue());
        assertEquals(11, limitSharing1.getBalance().intValue());
        LimitSharing limitSharing2 = limitSharingList.get(0);
        assertEquals(9, limitSharing2.getSum().intValue());
        assertEquals(9, limitSharing2.getBalance().intValue());

        List<String> auditList = depLimitService.auditLimits(organizationId, plannedYear, true);
        assertThat(auditList).isNotEmpty();
        assertThat(auditList.stream().filter(e -> e.indexOf("ERROR") > 0).count()).isZero();
        //get number of limits
        List<DepLimit> depLimitList2 = depLimitRepository.findAll();
        assertEquals(2, depLimitList2.size());
        //delete all limits
        Integer count = depLimitService.deleteLimits(organizationId, testEmployee1);
        assertEquals(2, count);
        //get number of limits
        List<DepLimit> depLimitList3 = depLimitRepository.findAll();
        assertEquals(0, depLimitList3.size());
    }

    @Test
    @DisplayName("Забор остатка лимита от дочернего подразделения к дочернему")
    @Disabled("Требуется переработка")
    void test_reShareLimitFromChildAndTakeBack() throws Exception { // NOSONAR
        // предусловие: подразделение, у него 2 дочерних, у них сотрудники
        // создать лимит организации первичный

        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR, 1);
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();
        int plannedMonth = plannedDate.getMonthValue();
        int plannedDay = plannedDate.getDayOfMonth();

        List<DepLimit> depLimitList1 = depLimitRepository.findAll();
        assertEquals(0, depLimitList1.size());

        var getLimitDTO = postAndGetLimit(plannedYear, BigDecimal.valueOf(80), false, "PASSENGER");

        UUID parentLimitId = getLimitDTO.getId();

        // распределить себе и на дочернее подразделение на такси
        postShareLimit(TransportTypeEnum.TAXI, BigDecimal.valueOf(80), parentLimitId);

        DepLimit mainLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(mainLimit).isNotNull();
        assertEquals(LimitStatus.PLANNING, mainLimit.getLimitStatus());
        assertEquals(80,
                mainLimit.getSum().intValue());
        assertEquals(0, mainLimit.getReserve().intValue());
        List<LimitSharing> limitSharingList = limitSharingService.getByLimit(mainLimit);
        assertEquals(1, limitSharingList.size());

        // share secondary
        List<DepLimitSharingPerTransportDTO> depLimitSharingPerTransportDTOList = new ArrayList<>();
        DepLimitSharingPerTransportDTO depLimitSharingPerTransportDTO =
                DepLimitSharingPerTransportDTO.builder()
                        .transportType(TransportTypeEnum.TAXI).sum(BigDecimal.valueOf(40)).build();
        depLimitSharingPerTransportDTOList.add(depLimitSharingPerTransportDTO);
        DepLimitSharingDTO secDto1 = DepLimitSharingDTO.builder()
                .sharingPerTransportList(depLimitSharingPerTransportDTOList)
                .targetDepartmentId(departmentChild1.getId())
                .finalSharing(false)
                .build();

        List<DepLimitSharingDTO> limitSharingDTOList11 = new ArrayList<>();
        limitSharingDTOList11.add(secDto1);
        String limitSharingStr11 = objectMapper.writeValueAsString(limitSharingDTOList11);

        mockMvc.perform(post("/deplimits/share_secondary/" + mainLimit.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr11))
                .andExpect(status().isOk());

        Limit limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitChild1.getLimitStatus());
        assertEquals(40, limitChild1.getSum().intValue());
        assertEquals(0, limitChild1.getChildren().size());

        List<DepLimit> limitList = depLimitService.getByDepartment(departmentChild1.getId());
        assertThat(limitList).isNotNull();
        assertEquals(1, limitList.size());

        // share secondary
        List<DepLimitSharingPerTransportDTO> depLimitSharingPerTransportDTOList2 = new ArrayList<>();
        DepLimitSharingPerTransportDTO depLimitSharingPerTransportDTO2 =
                DepLimitSharingPerTransportDTO.builder()
                        .transportType(TransportTypeEnum.TAXI).sum(BigDecimal.valueOf(20)).build();
        depLimitSharingPerTransportDTOList2.add(depLimitSharingPerTransportDTO2);
        DepLimitSharingDTO secDto2 = DepLimitSharingDTO.builder()
                .sharingPerTransportList(depLimitSharingPerTransportDTOList2)
                .targetDepartmentId(departmentChild1OfChild1.getId())
                .finalSharing(false)
                .build();
        List<DepLimitSharingDTO> limitSharingDTOList12 = new ArrayList<>();
        limitSharingDTOList12.add(secDto2);
        String limitSharingStr12 = objectMapper.writeValueAsString(limitSharingDTOList12);

        mockMvc.perform(post("/deplimits/share_secondary/" + limitChild1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr12))
                .andExpect(status().isOk());

        mainLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(mainLimit).isNotNull();
        assertEquals(LimitStatus.SHARED, mainLimit.getLimitStatus());
        assertEquals(80, mainLimit.getSum().intValue());
        assertEquals(0, mainLimit.getReserve().intValue());
        limitSharingList = limitSharingService.getByLimit(mainLimit);
        assertEquals(1, limitSharingList.size());
        assertEquals(40, limitSharingList.getFirst().getSum().intValue());
        var limitSharingPerPeriodList =
                limitSharingPerPeriodService.getByLimitSharing(limitSharingList.getFirst());
        assertEquals(12, limitSharingPerPeriodList.size());
        assertEquals(1, mainLimit.getChildren().size());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(20, limitChild1.getSum().intValue());
        List<LimitSharing> limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(20, limitSharingList1.getFirst().getSum().intValue());
        var limitSharingPerPeriodList1 =
                limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());
        assertEquals(1, limitChild1.getChildren().size());

        Limit limitChild1Child1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1OfChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1Child1).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitChild1Child1.getLimitStatus());
        assertEquals(20, limitChild1Child1.getSum().intValue());
        List<LimitSharing> limitSharingList2 = limitSharingService.getByLimit(limitChild1Child1);
        assertEquals(1,
                limitSharingList2.size());
        assertEquals(20, limitSharingList2.getFirst().getSum().intValue());
        var limitSharingPerPeriodList2 =
                limitSharingPerPeriodService.getByLimitSharing(limitSharingList2.getFirst());
        assertEquals(0, limitSharingPerPeriodList2.size());
        assertEquals(0, limitChild1Child1.getChildren().size());

        // забрать часть остатка у дочернего подразделения назад
        LimitResharingDTO limitResharingDTO = new LimitResharingDTO();
        limitResharingDTO.setSourceLimitId(limitChild1Child1.getId());
        limitResharingDTO.setSourceTransportType(TransportTypeEnum.TAXI);
        limitResharingDTO.setTargetLimitId(limitChild1.getId());
        limitResharingDTO.setTargetTransportType(TransportTypeEnum.TAXI);
        limitResharingDTO.setSum(BigDecimal.valueOf(10));

        String limitReSharingTakeFromChildDepartmentDTOStr = objectMapper.writeValueAsString(limitResharingDTO);

        mockMvc.perform(post("/deplimits/reshare_transfer")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitReSharingTakeFromChildDepartmentDTOStr))
                .andExpect(status().isOk());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(30, limitChild1.getSum().intValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(30, limitSharingList1.getFirst().getSum().intValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());

        limitChild1Child1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1OfChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1Child1).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitChild1Child1.getLimitStatus());
        assertEquals(10, limitChild1Child1.getSum().intValue());
        limitSharingList2 = limitSharingService.getByLimit(limitChild1Child1);
        assertEquals(1, limitSharingList2.size());
        assertEquals(10, limitSharingList2.getFirst().getSum().intValue());
        limitSharingPerPeriodList2 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList2.getFirst());
        assertEquals(0, limitSharingPerPeriodList2.size());

        List<String> auditList = depLimitService.auditLimits(organizationId, plannedYear, true);
        assertThat(auditList).isNotEmpty();
        assertThat(auditList.stream().filter(e -> e.indexOf("ERROR") > 0).count()).isZero();

        String sb = "/limits/exportstats" +
                "/" + organizationId +
                "/" + plannedYear +
                "/" + plannedMonth +
                "/" + plannedDay +
                "?" + departmentMain.getId();
        var response = mockMvc.perform(get(sb)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getResponse().getContentAsByteArray());
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        var iterator = workbook.getSheetAt(0).getRow(1).cellIterator();
        Cell cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isEqualTo(plannedYear);
        cell = iterator.next();
        assertThat(cell.getStringCellValue()).isEqualTo("moscow");
        cell = iterator.next();
        assertThat(cell.getStringCellValue()).isEmpty();
        cell = iterator.next();
        assertThat(cell.getStringCellValue()).isEmpty();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isEqualTo(3.0);
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isEqualTo(40);
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        iterator.next();
        //int value = Double.valueOf(cell.getNumericCellValue()).intValue();
        //assertTrue(value == -33 || value == -34);
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isEqualTo(50);
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isEqualTo(100);
        cell = iterator.next();
        assertThat(cell.getStringCellValue()).isEmpty();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isEqualTo(40);
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        iterator.next();
        //assertThat(cell.getNumericCellValue()).isEqualTo(-17);
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isZero();
        cell = iterator.next();
        assertThat(cell.getNumericCellValue()).isEqualTo(100);
    }

    @Test
    @DisplayName("Получение полных данных лимита по подразделению и году и типу услуги")
    void test_getByDepartmentAndYearAndLimitServiceTypeFull() throws Exception {
        // предусловие: подразделение, у него 2 дочерних, у них сотрудники
        // создать лимит организации первичный
        var now = Calendar.getInstance();
        now.add(Calendar.YEAR, 1);
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();

        List<DepLimit> depLimitList1 = depLimitRepository.findAll();
        assertEquals(0, depLimitList1.size());

        var getLimitPassengerDTO = postAndGetLimit(plannedYear, BigDecimal.valueOf(120), true, "PASSENGER");
        var getLimitCargoDTO = postAndGetLimit(plannedYear, BigDecimal.valueOf(240), true, "CARGO");

        var parentLimitPassengerId = getLimitPassengerDTO.getId();
        var parentLimitCargoId = getLimitCargoDTO.getId();

        // распределить на такси
        postShareLimit(TransportTypeEnum.TAXI, BigDecimal.valueOf(120), parentLimitPassengerId);

        // распределить на грузовик
        postShareLimit(TransportTypeEnum.DEDICATED, BigDecimal.valueOf(240), parentLimitCargoId);

        Limit limitMainPassenger = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(limitMainPassenger).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitMainPassenger.getLimitStatus());
        assertEquals(120, limitMainPassenger.getSum().intValue());
        List<LimitSharing> limitSharingPassengerList = limitSharingService.getByLimit(limitMainPassenger);
        assertEquals(1, limitSharingPassengerList.size());
        assertEquals(120, limitSharingPassengerList.getFirst().getSum().intValue());

        limitSharingPassengerList.forEach(it ->  limitSharingService.distributeSharingPerPeriod(it));

        var limitPassengerList = limitSharingService.distributeSharingPerPeriodImitation(limitMainPassenger, BigDecimal.valueOf(40));
        assertEquals(12, limitPassengerList.size());

        Limit limitMainCargo = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "CARGO");
        assertThat(limitMainCargo).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitMainCargo.getLimitStatus());
        assertEquals(240, limitMainCargo.getSum().intValue());
        List<LimitSharing> limitSharingCargoList = limitSharingService.getByLimit(limitMainCargo);
        assertEquals(1, limitSharingCargoList.size());
        assertEquals(240, limitSharingCargoList.getFirst().getSum().intValue());

        var limitCargoList = limitSharingService.distributeSharingPerPeriodImitation(limitMainCargo, BigDecimal.valueOf(40));
        assertEquals(12, limitCargoList.size());
        limitSharingCargoList.forEach(it ->  limitSharingService.distributeSharingPerPeriod(it));

        // get limit full passenger by department
        String getByDepartmentPassengerStr =
                "/deplimits/getByDepartmentAndYearAndLimitServiceTypeFull/" + departmentMain.getId() + "/year/" + plannedYear + "/limitServiceType/PASSENGER";
        var result = mockMvc.perform(get(getByDepartmentPassengerStr)
                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk());
        GetLimitDTO getLimitFullPassengerDTO = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                GetLimitDTO.class);
        assertEquals(getLimitFullPassengerDTO.getId(), parentLimitPassengerId);
        assertEquals(1, getLimitFullPassengerDTO.getLimitSharingDTOList().size());
        assertEquals(getLimitFullPassengerDTO.getLimitSharingDTOList().getFirst().getId(), limitSharingPassengerList.getFirst().getId());

        // get limit full cargo by department
        String getByDepartmentCargoStr =
                "/deplimits/getByDepartmentAndYearAndLimitServiceTypeFull/" + departmentMain.getId() + "/year/" + plannedYear + "/limitServiceType/CARGO";
        result = mockMvc.perform(get(getByDepartmentCargoStr)
                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk());
        GetLimitDTO getLimitFullCargoDTO = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                GetLimitDTO.class);
        assertEquals(getLimitFullCargoDTO.getId(), parentLimitCargoId);
        assertEquals(1, getLimitFullCargoDTO.getLimitSharingDTOList().size());
        assertEquals(getLimitFullCargoDTO.getLimitSharingDTOList().getFirst().getId(), limitSharingCargoList.getFirst().getId());
    }

    private GetLimitDTO postAndGetLimit(
            int year, BigDecimal sum,
            boolean useThisLimit, String limitServiceType
    ) throws Exception {
        var depLimitPrimaryDTO = DepLimitPrimaryDTO.builder()
                .year(year)
                .sum(sum)
                .limitSharingType(LimitSharingType.MONTHLY)
                .finalSharing(false)
                .useThisLimit(useThisLimit)
                .limitServiceType(limitServiceType)
                .build();
        var result = mockMvc.perform(post("/deplimits/add/" + organization1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(depLimitPrimaryDTO)))
                .andExpect(status().isOk());
        var getLimitDTO = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), GetLimitDTO.class);
        assertEquals(sum.stripTrailingZeros().toPlainString(), getLimitDTO.getSum().stripTrailingZeros().toPlainString());
        return getLimitDTO;
    }

    private void postShareLimit(TransportTypeEnum transportType, BigDecimal sum, UUID parentLimitId) throws Exception {
        var limitSharingDTO = PrimarySharingDTO.builder().transportType(transportType).sum(sum).build();
        mockMvc.perform(post("/deplimits/share_primary/" + parentLimitId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(limitSharingDTO))))
                .andExpect(status().isOk());
    }
}
