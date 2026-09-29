package ru.sberbank.ditsib.transport.limits.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.CommonTest;
import ru.sberbank.ditsib.transport.limits.constants.ImportStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.UpdateRule;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.dao.LimitRepository;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.EmpLimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPerPeriodService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.database.limits.Tables.SERVICES;
import static ru.sber.transport.database.limits.Tables.TYPES;
import static ru.sberbank.ditsib.transport.limits.constants.LimitStatus.PLANNING;
import static ru.sberbank.ditsib.transport.limits.constants.LimitStatus.SHARED;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера лимитов")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class LimitControllerTest extends CommonTest {
    @Autowired
    private DepLimitService depLimitService;
    @Autowired
    private EmpLimitService empLimitService;
    @Autowired
    private LimitRepository<DepLimit> depLimitRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private LimitRepository<Limit> limitRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LimitSharingService limitSharingService;
    @Autowired
    private LimitSharingPerPeriodService<Month> limitSharingPerPeriodService;

    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(authorizationManager, "ROLE_GUEST");
    }

    @BeforeEach
    public void init() {
        dslContext.insertInto(SERVICES)
                .values("PASSENGER")
                .onConflictDoNothing().execute();
        dslContext.insertInto(SERVICES)
                .values("CARGO")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("DEDICATED")
                .onConflictDoNothing().execute();
        dslContext.insertInto(TYPES)
                .values("TAXI")
                .onConflictDoNothing().execute();
        organization1 = organizationRepository.save(Organization.builder().id(organizationId).digitId(1L).build());
        departmentMain = departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .code("1111")
                .departmentName("moscow")
                .active(true)
                .organizationId(organization1.getId())
                .build());
        departmentChild1 = departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .organizationId(organization1.getId())
                .code("2222")
                .departmentName("voronej")
                .active(true)
                .parentId(departmentMain.getId())
                .build());
        departmentChild2 = departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .organizationId(organization1.getId())
                .code("3333")
                .departmentName("kursk")
                .active(true)
                .parentId(departmentMain.getId())
                .build());

        testEmployee1 = employeeRepository.save(Employee.builder()
                .id(userId1)
                .userId(userId1)
                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1)
                .personnelNumber("91111")
                .firstName("vasya")
                .lastName("pupkin")
                .organizationId(organization1.getId())
                .departmentId(departmentChild1.getId())
                .build());

        testEmployee2 = employeeRepository.save(Employee.builder()
                .id(userId2)
                .userId(userId2)
                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2)
                .personnelNumber("92222")
                .firstName("masha")
                .lastName("sidorova")
                .organizationId(organization1.getId())
                .departmentId(departmentChild1.getId())
                .build());
    }

    @AfterEach
    void clean() {
        limitSharingPerPeriodRepository.deleteAll();
        limitSharingRepository.deleteAll();
        depLimitRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    @SuppressWarnings("java:S5961")
    @Test
    @DisplayName("Поиск лимитов")
    void test_searchLimit() throws Exception {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR, 1);
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();

        List<DepLimit> depLimitList1 = depLimitRepository.findAll();
        assertEquals(0, depLimitList1.size());

        DepLimitPrimaryDTO limitDTO = new DepLimitPrimaryDTO();
        limitDTO.setYear(plannedYear);
        limitDTO.setSum(BigDecimal.valueOf(80));
        limitDTO.setLimitSharingType(LimitSharingType.MONTHLY);
        limitDTO.setParentId(null);
        limitDTO.setFinalSharing(false);
        limitDTO.setUseThisLimit(false);
        limitDTO.setLimitServiceType("PASSENGER");
        String depLimitStr = objectMapper.writeValueAsString(limitDTO);

        ResultActions result = mockMvc.perform(post("/deplimits/add/" + organization1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(depLimitStr))
                .andExpect(status().isOk());

        String contentAsString = result.andReturn().getResponse().getContentAsString();
        GetLimitDTO getLimitDTO = objectMapper.readValue(contentAsString, GetLimitDTO.class);
        assertEquals(80, getLimitDTO.getSum().intValue());
        UUID parentLimitId = getLimitDTO.getId();

        // распределить себе и на дочернее подразделение на такси
        PrimarySharingDTO limitSharingDTO1 =
                PrimarySharingDTO.builder().transportType(TransportTypeEnum.TAXI).sum(BigDecimal.valueOf(80)).build();
        List<PrimarySharingDTO> dtoList = new ArrayList<>();
        dtoList.add(limitSharingDTO1);
        String limitSharingStr = objectMapper.writeValueAsString(dtoList);

        mockMvc.perform(post("/deplimits/share_primary/" + parentLimitId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr))
                .andExpect(status().isOk());

        DepLimit limitMain = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(limitMain).isNotNull();
        assertEquals(PLANNING, limitMain.getLimitStatus());
        assertEquals(80, limitMain.getSum().intValue());
        assertEquals(0, limitMain.getReserve().intValue());
        List<LimitSharing> limitSharingList = limitSharingService.getByLimit(limitMain);
        assertEquals(1, limitSharingList.size());

        // share secondary
        List<DepLimitSharingPerTransportDTO> depLimitSharingPerTransportDTOList = new ArrayList<>();
        DepLimitSharingPerTransportDTO depLimitSharingPerTransportDTO =
                DepLimitSharingPerTransportDTO.builder()
                        .transportType(TransportTypeEnum.TAXI).sum(BigDecimal.valueOf(60)).build();
        depLimitSharingPerTransportDTOList.add(depLimitSharingPerTransportDTO);
        DepLimitSharingDTO secDto1 = DepLimitSharingDTO.builder()
                .sharingPerTransportList(depLimitSharingPerTransportDTOList)
                .targetDepartmentId(departmentChild1.getId())
                .finalSharing(false)
                .build();
        List<DepLimitSharingDTO> limitSharingDTOList10 = new ArrayList<>();
        limitSharingDTOList10.add(secDto1);

        String limitSharingStr10 = objectMapper.writeValueAsString(limitSharingDTOList10);
        mockMvc.perform(post("/deplimits/share_secondary/" + limitMain.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr10))
                .andExpect(status().isOk());

        Limit limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(PLANNING, limitChild1.getLimitStatus());
        assertEquals(60, limitChild1.getSum().intValue());
        List<LimitSharing> limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(60, limitSharingList1.getFirst().getSum().intValue());
        var limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(0, limitSharingPerPeriodList1.size());

        // share child limit
        String limitSharingStr11 = objectMapper.writeValueAsString(new ArrayList<>());
        mockMvc.perform(post("/deplimits/share_secondary/" + limitChild1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr11))
                .andExpect(status().isOk());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(SHARED, limitChild1.getLimitStatus());
        assertEquals(60, limitChild1.getSum().intValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(60, limitSharingList1.getFirst().getSum().intValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(SHARED, limitChild1.getLimitStatus());

        // выдать лимит сотруднику
        EmpLimitSharingDTO limitSharingDTO12 = new EmpLimitSharingDTO();
        limitSharingDTO12.setYear(plannedYear);
        limitSharingDTO12.setTransportType(TransportTypeEnum.TAXI);
        limitSharingDTO12.setSum(BigDecimal.valueOf(30));
        limitSharingDTO12.setTargetEmployeeId(testEmployee1.getId());
        String limitSharingStr12 = objectMapper.writeValueAsString(limitSharingDTO12);
        mockMvc.perform(post("/emplimits/make_emp_limit")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr12))
                .andExpect(status().isOk());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(SHARED, limitChild1.getLimitStatus());
        assertEquals(30, limitChild1.getSum().intValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(30, limitSharingList1.getFirst().getSum().intValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());

        EmpLimit limitEmployee1 = empLimitService.getByEmployeeAndYear(testEmployee1, plannedYear);
        assertThat(limitEmployee1).isNotNull();
        assertEquals(SHARED, limitEmployee1.getLimitStatus());
        assertEquals(30, limitEmployee1.getSum().intValue());
        limitSharingList1 = limitSharingService.getByLimit(limitEmployee1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(30, limitSharingList1.getFirst().getSum().intValue());

        //List<LimitSharing> limitSharingListx1 = limitSharingService.getByLimit(limitMain);
        //List<LimitSharing> limitSharingListx2 = limitSharingService.getByLimit(limitChild1);
        //List<LimitSharing> limitSharingListx3 = limitSharingService.getByLimit(limitEmployee1);
        // *********** search for limit *****************
        LimitSearchDTO limitSearchDTO0 = new LimitSearchDTO();
        String limitSearchDTOStr0 = objectMapper.writeValueAsString(limitSearchDTO0);
        var response0 = mockMvc.perform(post("/limits/search/" + organizationId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSearchDTOStr0))
                .andExpect(status().isOk()).andReturn();
        List<GetLimitDTO> found0 =
                objectMapper.readValue(response0.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(3, found0.size());
        // *********** search for limit *****************
        LimitSearchDTO limitSearchDTO1 = new LimitSearchDTO();
        limitSearchDTO1.setYear(plannedYear);
        limitSearchDTO1.setLimitType(LimitType.DEPARTMENT);
        limitSearchDTO1.setLimitServiceType("PASSENGER");
        limitSearchDTO1.setLimitStatus(SHARED);
        String limitSearchDTOStr1 = objectMapper.writeValueAsString(limitSearchDTO1);
        var response1 = mockMvc.perform(post("/limits/search/" + organizationId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSearchDTOStr1))
                .andExpect(status().isOk()).andReturn();
        List<GetLimitDTO> found1 =
                objectMapper.readValue(response1.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(2, found1.size());
        // **************** search test 2 ****************
        LimitSearchDTO limitSearchDTO2 = new LimitSearchDTO();
        limitSearchDTO2.setHumanReadableLimitId(limitChild1.getHumanReadableId());
        //limitSearchDTO2.setParentDepartment(limitChild1.getParentDepartment().getDepartmentName());
        String limitSearchDTOStr2 = objectMapper.writeValueAsString(limitSearchDTO2);
        var response2 = mockMvc.perform(post("/limits/search/" + organizationId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSearchDTOStr2))
                .andExpect(status().isOk()).andReturn();
        List<GetLimitDTO> found2 =
                objectMapper.readValue(response2.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(1, found2.size());
        assertEquals(limitChild1.getHumanReadableId(), found2.getFirst().getHumanReadableId());
        // **************** search test 2 ****************
        LimitSearchDTO limitSearchDTO3 = new LimitSearchDTO();
        limitSearchDTO3.setLimitId(limitChild1.getId());
        String limitSearchDTOStr3 = objectMapper.writeValueAsString(limitSearchDTO3);
        mockMvc.perform(post("/limits/search/" + organizationId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSearchDTOStr3))
                .andExpect(status().isOk()).andReturn();
        List<GetLimitDTO> found3 =
                objectMapper.readValue(response2.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(1, found3.size());
        assertEquals(limitChild1.getId(), found3.getFirst().getId());
        // ****************** audit test **********************
        List<String> auditList = depLimitService.auditLimits(organizationId, plannedYear, true);
        assertThat(auditList).isNotEmpty();
        assertThat(auditList.stream().filter(e -> e.indexOf("ERROR") > 0).count()).isZero();
    }

    @Test
    @DisplayName("Поиск лимитов по частичному совпадению строки запроса")
    @Transactional
    void searchLimitsTest() throws Exception {

        final DepLimit depLimit1 = new DepLimit();
        depLimit1.setYear(2023);
        depLimit1.setSum(BigDecimal.valueOf(80));
        depLimit1.setLimitSharingType(LimitSharingType.MONTHLY);
        depLimit1.setParent(null);
        depLimit1.setDepartment(departmentMain);
        depLimit1.setLimitOwner(testEmployee1);
        depLimit1.setAuthor(testEmployee1);
        depLimit1.setLimitType(LimitType.DEPARTMENT);
        depLimit1.setLimitStatus(SHARED);
        depLimit1.setFinalSharing(true);
        depLimit1.setUseThisLimit(true);
        depLimit1.setOrganization(organization1);
        depLimit1.setLimitServiceType("PASSENGER");
        depLimit1.setReserve(BigDecimal.valueOf(10));
        depLimit1.setEconomy(BigDecimal.valueOf(10));
        depLimit1.setHumanReadableId("first");
        depLimit1.setCreationTime(LocalDateTime.now());

        DepLimit depLimitInDb1 = depLimitRepository.save(depLimit1);

        final DepLimit depLimit2 = new DepLimit();
        depLimit2.setYear(2024);
        depLimit2.setCreationTime(LocalDateTime.now());
        depLimit2.setSum(BigDecimal.valueOf(80L));
        depLimit2.setLimitSharingType(LimitSharingType.MONTHLY);
        depLimit2.setParent(null);
        depLimit2.setDepartment(departmentMain);
        depLimit2.setLimitOwner(testEmployee1);
        depLimit2.setAuthor(testEmployee1);
        depLimit2.setLimitType(LimitType.DEPARTMENT);
        depLimit2.setLimitStatus(SHARED);
        depLimit2.setFinalSharing(true);
        depLimit2.setUseThisLimit(true);
        depLimit2.setOrganization(organization1);
        depLimit2.setLimitServiceType("PASSENGER");
        depLimit2.setReserve(BigDecimal.valueOf(10L));
        depLimit2.setEconomy(BigDecimal.valueOf(10L));
        depLimit2.setHumanReadableId("second");

        var depLimitInDb2 = depLimitRepository.save(depLimit2);

        // Поиск лимита по части строки humanReadableId

        LimitSearchDTO searchDTO = LimitSearchDTO.builder()
                .humanReadableLimitId("eco").build();

        String contentForSearch = objectMapper.writeValueAsString(searchDTO);

        var respListLimits = mockMvc.perform(
                        post("/limits/search/" + organizationId)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(contentForSearch))
                .andExpect(status().isOk()).andReturn();

        List<GetLimitDTO> limitDTOList =
                objectMapper.readValue(respListLimits.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });

        assertEquals(1, limitDTOList.size());
        assertEquals(depLimitInDb2.getHumanReadableId(), limitDTOList.getFirst().getHumanReadableId());

        // Поиск лимтов по части имени владельца лимита
        Employee limitOwner = employeeRepository.findById(depLimitInDb1.getLimitOwner().getId()).orElseThrow();

        String firstName = limitOwner.getFirstName();
        String strForSearch2 = firstName.substring(firstName.length() - 3);

        var DtoForSearchBySubStrName = LimitSearchDTO.builder()
                .limitOwner(strForSearch2).build();


        String contentForSearch2 = objectMapper.writeValueAsString(DtoForSearchBySubStrName);

        var response5 = mockMvc.perform(
                        post("/limits/search/" + organizationId)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(contentForSearch2))
                .andExpect(status().isOk()).andReturn();

        List<GetLimitDTO> limitDTOList1 =
                objectMapper.readValue(response5.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });

        assertEquals(2, limitDTOList1.size());
        assertEquals(limitOwner.getId(), limitDTOList1.getFirst().getLimitOwner());
        assertEquals(limitOwner.getId(), limitDTOList1.get(1).getLimitOwner());
    }

    @Test
    @DisplayName("Загрузка лимитов")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void importLimitsTest() throws Exception {
        String sb = "/limits/import/" + ORGANIZATION_ID +
                "?updateRule=" + UpdateRule.ADD;
        String originalFileName = "import_limits.xlsx";
        String path = "excel/";
        MockMultipartFile file = new MockMultipartFile("file", originalFileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getFileFromResource(path, originalFileName));
        ResultActions result = mockMvc.perform(multipart(sb).file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());
        ImportResultDTO importResultDTO = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                ImportResultDTO.class);
        assertEquals(0, importResultDTO.getErrors());
        assertEquals(ImportStatus.SUCCESS, importResultDTO.getResultStatus());
    }

    @Test
    @DisplayName("Загрузка лимитов (грузы)")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void importLimitsCargoTest() throws Exception {
        var sb = "/limits/import/%s?updateRule=%s".formatted(ORGANIZATION_ID, UpdateRule.ADD);
        var originalFileName = "import_limits_cargo.xlsx";
        var path = "excel/";
        var file = new MockMultipartFile("file", originalFileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getFileFromResource(path, originalFileName));
        var result = mockMvc.perform(multipart(sb).file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());
        var importResultDTO = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                ImportResultDTO.class);
        assertEquals(0, importResultDTO.getErrors());
        assertEquals(ImportStatus.SUCCESS, importResultDTO.getResultStatus());

        var actualList = limitRepository.findByOrganizationIdAndYear(UUID.fromString(ORGANIZATION_ID), 2021);
        assertTrue(actualList.stream().allMatch(limit -> Objects.equals(limit.getLimitServiceType(), "CARGO")));
    }

    private ByteArrayInputStream getFileFromResource(String pathParam, String originalFileName) throws IOException {
        ClassLoader classLoader = LimitControllerTest.class.getClassLoader();
        String pathFileName = pathParam + originalFileName;
        URL url = classLoader.getResource(pathFileName);
        if (url != null) {
            File file = new File(url.getFile());
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new ByteArrayInputStream(bytes);
        } else {
            throw new FileNotFoundException(pathFileName);
        }
    }

}
