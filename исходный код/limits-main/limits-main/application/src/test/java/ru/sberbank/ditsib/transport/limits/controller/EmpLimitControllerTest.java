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
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.CommonTest;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.EmpLimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPerPeriodService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера личных лимитов")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class EmpLimitControllerTest extends CommonTest {
    @Autowired
    private EmpLimitService empLimitService;
    @Autowired
    private DepLimitService depLimitService;
    @Autowired
    private EmpLimitRepository empLimitRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private LimitRepository<DepLimit> depLimitRepository;
    @Autowired
    private LimitSharingService limitSharingService;
    @Autowired
    private LimitSharingPerPeriodService<Month> limitSharingPerPeriodService;
    @Autowired
    private MockMvc mockMvc;

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
        dslContext.insertInto(TYPES)
                .values("TAXI")
                .onConflictDoNothing().execute();
        dslContext.insertInto(SERVICES)
                .values("CARGO")
                .onConflictDoNothing().execute();

        organization1 = Organization.builder().id(organizationId).digitId(1L).build();
        departmentMain = Department.builder()
                                   .id(UUID.randomUUID())
                                   .organizationId(organization1.getId())
                                   .code("1111")
                                   .active(true)
                                   .departmentName("moscow")
                                   .build();
        departmentChild1 = Department.builder()
                                     .id(UUID.randomUUID())
                                     .organizationId(organization1.getId())
                                     .code("2222")
                                     .active(true)
                                     .departmentName("voronej")
                                     .parentId(departmentMain.getId())
                                     .build();
        departmentChild2 = Department.builder()
                                     .id(UUID.randomUUID())
                                     .organizationId(organization1.getId())
                                     .code("3333")
                                     .active(true)
                                     .departmentName("kursk")
                                     .parentId(departmentMain.getId())
                                     .build();
        
        testEmployee1 = Employee.builder()
                                .id(userId1)
                                .userId(userId1)
                                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1)
                                .personnelNumber("91111")
                                .firstName("vasya")
                                .lastName("pupkin")
                                .organizationId(organization1.getId())
                                .departmentId(departmentChild1.getId())
                                .build();
        
        testEmployee2 = Employee.builder()
                                .id(userId2)
                                .userId(userId2)
                                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2)
                                .personnelNumber("92222")
                                .firstName("masha")
                                .lastName("sidorova")
                                .organizationId(organization1.getId())
                                .departmentId(departmentChild1.getId())
                                .build();
        
        organizationRepository.saveAndFlush(organization1);
        
        departmentRepository.saveAndFlush(departmentMain);
        departmentRepository.saveAndFlush(departmentChild1);
        departmentRepository.saveAndFlush(departmentChild2);
        
        employeeRepository.saveAndFlush(testEmployee1);
        employeeRepository.saveAndFlush(testEmployee2);
        
        testEmployee1.setSupervisorId(testEmployee2.getId());
        employeeRepository.saveAndFlush(testEmployee1);
        
        departmentMain.setDepartmentHead(testEmployee1);
        departmentRepository.saveAndFlush(departmentMain);
        departmentChild1.setDepartmentHead(testEmployee1);
        departmentRepository.saveAndFlush(departmentChild1);
        departmentChild2.setDepartmentHead(testEmployee1);
        departmentRepository.saveAndFlush(departmentChild2);
    }

    @AfterEach
    void tearDown() {
        limitSpendingRepository.deleteAll();
        limitSharingPerPeriodRepository.deleteAll();
        limitSharingRepository.deleteAll();
        depLimitRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }
    
    @Test
    @DisplayName("CRUD личных лимитов")
    @WithMockUser(username=USER1_ID, roles="GUEST")
    void test_CRUD() throws Exception {
        List<EmpLimit> empLimitList1 = empLimitRepository.findAll();
        assertEquals(0, empLimitList1.size());
        
        EmpLimit empLimit = new EmpLimit();
        empLimit.setAuthor(testEmployee1);
        empLimit.setYear(2020);
        empLimit.setSum(BigDecimal.valueOf(100));
        empLimit.setLimitSharingType(LimitSharingType.MONTHLY);
        empLimit.setLimitServiceType("PASSENGER");
        empLimit.setFinalSharing(false);
        empLimit.setUseThisLimit(false);
        empLimit.setLimitOwner(testEmployee1);
        empLimit.setLimitStatus(LimitStatus.PLANNING);
        empLimit.setParent(null);
        empLimit.setEmployee(testEmployee1);
        empLimit.setOrganization(organization1);
        empLimit.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        empLimit = empLimitService.add(empLimit);
        
        assertEquals(100, empLimit.getSum().longValue());
        
        List<EmpLimit> empLimitList2 = empLimitRepository.findAll();
        assertEquals(1, empLimitList2.size());
        
        empLimit.setYear(empLimit.getYear());
        empLimit.setSum(BigDecimal.valueOf(200));
        empLimit.setLimitSharingType(empLimit.getLimitSharingType());
        empLimit.setFinalSharing(empLimit.isFinalSharing());
        empLimit.setUseThisLimit(empLimit.isUseThisLimit());
        empLimitService.save(empLimit);
        
        String url = "/emplimits/" + empLimit.getId();
        
        //perform get
        mockMvc.perform(get(url))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.sum").value("20000"))
               .andExpect(jsonPath("$.id").value(empLimit.getId().toString()));
        
        //perform delete
        empLimitService.delete(empLimitService.get(empLimit.getId()).orElse(null));
        
        List<EmpLimit> empLimitList3 = empLimitRepository.findAll();
        assertEquals(0, empLimitList3.size());
    }
    
    @Test
    @DisplayName("Получение всех личных лимитов")
    @WithMockUser(username=USER1_ID, roles="GUEST")
    void test_getAll() throws Exception {
        var response = mockMvc.perform(
                                      get("/emplimits")
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        List<GetLimitDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(empLimitRepository.findAll().size(), actual.size());
    }
    
    @Test
    @DisplayName("Выделение изменение и удаление лимита сотрудника")
    void test_make_change_delete_EmployeeLimit() throws Exception { // NOSONAR
        // предусловие: подразделение, у него 2 дочерних, у них сотрудники
        // создать лимит на родительское подразделение на такси
        // выделить сотруднику подразделения лимит на такси
        // поменять его лимит в сторону увеличения, затем уменьшения
        // удалить его лимит
        
        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR,1);
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();
        
        List<Department> departmentList = departmentRepository.findAll();
        assertEquals(3, departmentList.size());
        
        List<DepLimit> depLimitList1 = depLimitRepository.findAll();
        assertEquals(0, depLimitList1.size());
        
        DepLimitPrimaryDTO limitDTO = new DepLimitPrimaryDTO();
        limitDTO.setYear(plannedYear);
        limitDTO.setSum(BigDecimal.valueOf(80));
        limitDTO.setLimitSharingType(LimitSharingType.MONTHLY);
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
        assertEquals(80, getLimitDTO.getSum().longValue());
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
        assertEquals(LimitStatus.PLANNING, limitMain.getLimitStatus());
        assertEquals(80, limitMain.getSum().longValue());
        assertEquals(0, limitMain.getReserve().longValue());
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
        
        Limit limitChild1;
        List<LimitSharing> limitSharingList1;
        Map<Month, LimitSharingPerPeriod> limitSharingPerPeriodList1;
        
        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitChild1.getLimitStatus());
        assertEquals(60, limitChild1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(60, limitSharingList1.getFirst().getSum().longValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(0, limitSharingPerPeriodList1.size());
        limitMain = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertEquals(1, limitMain.getChildren().size());
        
        // share child limit
        String limitSharingStr11 = objectMapper.writeValueAsString(new ArrayList<>());
        mockMvc.perform(post("/deplimits/share_secondary/" + limitChild1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(limitSharingStr11))
               .andExpect(status().isOk());
        
        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(60, limitChild1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(60, limitSharingList1.getFirst().getSum().longValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());
        
        // выдать лимит сотруднику
        EmpLimitSharingDTO limitSharingDTO12 = new EmpLimitSharingDTO();
        limitSharingDTO12.setYear(plannedYear);
        limitSharingDTO12.setTransportType(TransportTypeEnum.TAXI);
        limitSharingDTO12.setSum(BigDecimal.valueOf(30));
        limitSharingDTO12.setTargetEmployeeId(testEmployee1.getId());
        
        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());

        String limitSharingStr12 = objectMapper.writeValueAsString(limitSharingDTO12);
        mockMvc.perform(post("/emplimits/make_emp_limit")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                                         .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                         .content(limitSharingStr12))
               .andExpect(status().isOk());
        
        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(30, limitChild1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(30, limitSharingList1.getFirst().getSum().longValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());
        assertEquals(1, limitChild1.getChildren().size());
        
        EmpLimit limitEmployee1 = empLimitService.getByEmployeeAndYear(testEmployee1, plannedYear);
        assertThat(limitEmployee1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitEmployee1.getLimitStatus());
        assertEquals(30, limitEmployee1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitEmployee1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(30, limitSharingList1.getFirst().getSum().longValue());
        
        // выдать еще лимит сотруднику
        EmpLimitSharingDTO limitSharingDTO13 = new EmpLimitSharingDTO();
        limitSharingDTO13.setYear(plannedYear);
        limitSharingDTO13.setTransportType(TransportTypeEnum.TAXI);
        limitSharingDTO13.setSum(BigDecimal.valueOf(10));
        limitSharingDTO13.setTargetEmployeeId(testEmployee1.getId());
        
        String limitSharingStr13 = objectMapper.writeValueAsString(limitSharingDTO13);
        mockMvc.perform(put("/emplimits/change_emp_limit")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .content(limitSharingStr13))
               .andExpect(status().isOk());
        
        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(20, limitChild1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(20, limitSharingList1.getFirst().getSum().longValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());
        
        limitEmployee1 = empLimitService.getByEmployeeAndYear(testEmployee1, plannedYear);
        assertThat(limitEmployee1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitEmployee1.getLimitStatus());
        assertEquals(40, limitEmployee1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitEmployee1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(40, limitSharingList1.getFirst().getSum().longValue());
        // get limit by department
        String getByDepartmentStr =
                "/deplimits/getByDepartmentAndYearAndLimitServiceType/" + departmentChild1.getId() + "/year/" + plannedYear + "/limitServiceType/" + "PASSENGER";
        result = mockMvc.perform(get(getByDepartmentStr)
                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());
        GetLimitDTO getLimitDTO2 = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                                                          GetLimitDTO.class);
        assertEquals(getLimitDTO2.getId(), limitChild1.getId());
        
        // get limit by employee
        String getByEmployeeStr = "/emplimits/getByEmployeeAndYear/" + testEmployee1.getId() + "/year/" + plannedYear;
        result = mockMvc.perform(get(getByEmployeeStr)
                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());
        GetLimitDTO getLimitDTO3 = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                                                          GetLimitDTO.class);
        assertEquals(getLimitDTO3.getId(), limitEmployee1.getId());
        
        // удалить лимит сотрудника
        mockMvc.perform(put("/emplimits/close_emp_limit/" + limitEmployee1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk());
        
        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(60, limitChild1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(60, limitSharingList1.getFirst().getSum().longValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());
        
        limitEmployee1 = empLimitService.getByEmployeeAndYear(testEmployee1, plannedYear);
        assertThat(limitEmployee1).isNull();
        
        //--------------------------------------------------------------
        // выдать лимит сотруднику заново
        EmpLimitSharingDTO limitSharingDTO14 = new EmpLimitSharingDTO();
        limitSharingDTO14.setYear(plannedYear);
        limitSharingDTO14.setTransportType(TransportTypeEnum.TAXI);
        limitSharingDTO14.setSum(BigDecimal.valueOf(36));
        limitSharingDTO14.setTargetEmployeeId(testEmployee1.getId());
        String limitSharingStr14 = objectMapper.writeValueAsString(limitSharingDTO14);
        
        mockMvc.perform(post("/emplimits/make_emp_limit")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                                         .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                         .content(limitSharingStr14))
               .andExpect(status().isOk());
        
        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(24, limitChild1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitChild1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(24, limitSharingList1.getFirst().getSum().longValue());
        limitSharingPerPeriodList1 = limitSharingPerPeriodService.getByLimitSharing(limitSharingList1.getFirst());
        assertEquals(12, limitSharingPerPeriodList1.size());
        
        limitEmployee1 = empLimitService.getByEmployeeAndYear(testEmployee1, plannedYear);
        assertThat(limitEmployee1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitEmployee1.getLimitStatus());
        assertEquals(36, limitEmployee1.getSum().longValue());
        limitSharingList1 = limitSharingService.getByLimit(limitEmployee1);
        assertEquals(1, limitSharingList1.size());
        assertEquals(36, limitSharingList1.getFirst().getSum().longValue());
        //--------- audit limits
        List<String> auditList = depLimitService.auditLimits(organizationId, plannedYear, true);
        assertThat(auditList).isNotEmpty();
        assertThat(auditList.stream().filter(e -> e.indexOf("ERROR") > 0).count()).isZero();
        //-----------------------------------------------------------
        
        var response = mockMvc.perform(get("/emplimits")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                                 .contentType(MediaType.APPLICATION_JSON_VALUE))
                                                 .andExpect(status().isOk()).andReturn();
        List<GetLimitDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(empLimitRepository.findAll().size(), actual.size());
        
        var response2 = mockMvc.perform(get("/limittransferhistory")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                                               .andExpect(status().isOk()).andReturn();
        List<GetLimitTransferHistoryDTO> actual2 =
                objectMapper.readValue(response2.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(5, actual2.size());
    }
}
