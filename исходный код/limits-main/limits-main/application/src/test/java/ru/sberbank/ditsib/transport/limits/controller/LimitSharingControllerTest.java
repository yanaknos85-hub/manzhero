package ru.sberbank.ditsib.transport.limits.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.CommonTest;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingRepository;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@Transactional
@DisplayName("Проверка контроллера распределения лимитов по видам транспорта")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class LimitSharingControllerTest extends CommonTest {
    @Autowired
    private DepLimitService depLimitService;
    @Autowired
    private LimitSharingService limitSharingService;
    @Autowired
    private LimitSharingRepository limitSharingRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
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
                .execute();
        dslContext.insertInto(TYPES)
                .values("TAXI")
                .execute();
        dslContext.insertInto(SERVICES)
                .values("CARGO")
                .execute();
        organization1 = Organization.builder().id(UUID.randomUUID()).digitId(1L).build();
        departmentMain = Department.builder()
                                   .id(UUID.randomUUID())
                                   .organizationId(organization1.getId())
                                   .code("1111")
                                   .departmentName("moscow")
                                   .active(true)
                                   .build();
        departmentChild1 = Department.builder()
                                     .id(UUID.randomUUID())
                                     .organizationId(organization1.getId())
                                     .code("2222")
                                     .departmentName("voronej")
                                     .active(true)
                                     .parentId(departmentMain.getId())
                                     .build();
        departmentChild2 = Department.builder()
                                     .id(UUID.randomUUID())
                                     .organizationId(organization1.getId())
                                     .code("3333")
                                     .departmentName("kursk")
                                     .active(true)
                                     .parentId(departmentMain.getId())
                                     .build();
        
        testEmployee1 = Employee.builder()
                                .id(userId1)
                                .userId(userId1)
                                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1)
                                .firstName("vasya")
                                .lastName("pupkin")
                                .departmentId(departmentChild1.getId())
                                .build();
        
        testEmployee2 = Employee.builder()
                                .id(userId2)
                                .userId(userId2)
                                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2)
                                .firstName("masha")
                                .lastName("sidorova")
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
        
        departmentChild1.setDepartmentHead(testEmployee1);
        departmentRepository.saveAndFlush(departmentChild1);
    }
    
    @Test
    @DisplayName("CRUD распределения лимитов по видам транспорта")
    @WithMockUser(username=USER1_ID, roles="GUEST")
    void test_CRUD() throws Exception {
        List<LimitSharing> limitSharingList1 = limitSharingRepository.findAll();
        assertEquals(0, limitSharingList1.size());

        final var service = dslContext.insertInto(SERVICES)
                .values(Instancio.create(String.class))
                .returning(SERVICES.ID)
                .fetchSingle(SERVICES.ID);
        
        DepLimit depLimit = new DepLimit();
        depLimit.setOrganization(organization1);
        depLimit.setYear(1974);
        depLimit.setSum(BigDecimal.valueOf(1000));
        depLimit.setAuthor(testEmployee1);
        depLimit.setReserve(BigDecimal.valueOf(1000));
        depLimit.setEconomy(BigDecimal.valueOf(0));
        depLimit.setLimitSharingType(LimitSharingType.MONTHLY);
        depLimit.setLimitOwner(testEmployee1);
        depLimit.setLimitStatus(LimitStatus.PLANNING);
        depLimit.setParent(null);
        depLimit.setDepartment(departmentChild1);
        depLimit.setFinalSharing(false);
        depLimit.setUseThisLimit(false);
        depLimit.setLimitServiceType(service);
        depLimit = depLimitService.add(depLimit);
        
        LimitSharing limitSharing = new LimitSharing();
        limitSharing.setAuthor(testEmployee1);
        limitSharing.setTransportType(TransportTypeEnum.TAXI);
        limitSharing.setSum(BigDecimal.valueOf(1000));
        limitSharing.setBalance(BigDecimal.valueOf(1000));
        limitSharing.setLimit(depLimit);
        limitSharing = limitSharingService.add(limitSharing);
        assertEquals(1000, limitSharing.getSum().intValue());
        
        List<LimitSharing> limitSharingList2 = limitSharingRepository.findAll();
        assertEquals(1, limitSharingList2.size());
        
        limitSharing.setTransportType(TransportTypeEnum.PUBLIC);
        limitSharing.setSum(BigDecimal.valueOf(2000));
        limitSharingService.save(limitSharing);
        
        //perform get
        String url = "/limitsharing/" + limitSharing.getId();
        mockMvc.perform(get(url))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.sum").value("200000"))
               .andExpect(jsonPath("$.id").value(limitSharing.getId().toString()));
        
        //perform delete
        limitSharingService.delete(limitSharingService.get(limitSharing.getId()).orElse(null));
        
        List<LimitSharing> limitSharingList3 = limitSharingRepository.findAll();
        assertEquals(0, limitSharingList3.size());
    }
    
    @Test
    @DisplayName("Получение всех распределений лимитов по видам транспорта")
    @WithMockUser(username=USER1_ID, roles="GUEST")
    void test_getAll() throws Exception {
        var response = mockMvc.perform(
                                      get("/limitsharing")
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        List<GetLimitSharingDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(limitSharingRepository.findAll().size(), actual.size());
    }
}
