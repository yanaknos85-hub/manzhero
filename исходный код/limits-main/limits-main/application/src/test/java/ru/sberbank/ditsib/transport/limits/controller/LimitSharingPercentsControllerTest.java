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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.CommonTest;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.dao.LimitSharingPercentsRepository;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitSharingPercentsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitSharingPercentsDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPercents;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.database.limits.Tables.SERVICES;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка процентного распределения лимитов")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class LimitSharingPercentsControllerTest extends CommonTest {
    @Autowired
    private DepLimitService depLimitService;
    @Autowired
    private LimitSharingPercentsRepository limitSharingProcentsRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
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
    @DisplayName("CRUD процентного распределения лимитов")
    void test_CRUD() throws Exception {
        employeeRepository.findAll();
        List<LimitSharingPercents> limitSharingPercentsList1 = limitSharingProcentsRepository.findAll();
        assertEquals(0, limitSharingPercentsList1.size());

        final var service = dslContext.insertInto(SERVICES)
                .values(Instancio.create(String.class))
                .returning(SERVICES.ID)
                .fetchSingle(SERVICES.ID);
        
        DepLimit depLimit = new DepLimit();
        depLimit.setYear(1974);
        depLimit.setSum(BigDecimal.valueOf(1000));
        depLimit.setAuthor(testEmployee1);
        depLimit.setReserve(BigDecimal.valueOf(1000));
        depLimit.setEconomy(BigDecimal.valueOf(0));
        depLimit.setLimitSharingType(LimitSharingType.PERCENTS);
        depLimit.setLimitOwner(testEmployee1);
        depLimit.setLimitStatus(LimitStatus.PLANNING);
        depLimit.setParent(null);
        depLimit.setDepartment(departmentChild1);
        depLimit.setOrganization(organization1);
        depLimit.setFinalSharing(false);
        depLimit.setUseThisLimit(false);
        depLimit.setLimitServiceType(service);
        depLimit = depLimitService.add(depLimit);
        
        var limitSharingPercentsDTO = new LimitSharingPercentsDTO();
        limitSharingPercentsDTO.setLimitId(depLimit.getId());
        limitSharingPercentsDTO.setJanuary(10);
        limitSharingPercentsDTO.setFebruary(8);
        limitSharingPercentsDTO.setMarch(8);
        limitSharingPercentsDTO.setApril(8);
        limitSharingPercentsDTO.setMay(8);
        limitSharingPercentsDTO.setJune(8);
        limitSharingPercentsDTO.setJuly(8);
        limitSharingPercentsDTO.setAugust(8);
        limitSharingPercentsDTO.setSeptember(8);
        limitSharingPercentsDTO.setOctober(8);
        limitSharingPercentsDTO.setNovember(8);
        limitSharingPercentsDTO.setDecember(10);
        String limitSharingProcentsStr = objectMapper.writeValueAsString(limitSharingPercentsDTO);
        
        ResultActions result = mockMvc.perform(post("/limitsharingprocents")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                                                            .contentType(
                                                                                    MediaType.APPLICATION_JSON_VALUE)
                                                                            .content(limitSharingProcentsStr))
                                      .andExpect(status().isOk());
        
        String contentAsString = result.andReturn().getResponse().getContentAsString();
        GetLimitSharingPercentsDTO
                getLimitSharingPercentsDTO = objectMapper.readValue(contentAsString, GetLimitSharingPercentsDTO.class);
        
        assertEquals(10, getLimitSharingPercentsDTO.getJanuary());
        
        List<LimitSharingPercents> limitSharingPercentsList2 = limitSharingProcentsRepository.findAll();
        assertEquals(1, limitSharingPercentsList2.size());
        
        LimitSharingPercentsDTO limitSharingPercentsDTO2 = new LimitSharingPercentsDTO();
        limitSharingPercentsDTO2.setJanuary(8);
        limitSharingPercentsDTO2.setFebruary(10);
        limitSharingPercentsDTO2.setMarch(8);
        limitSharingPercentsDTO2.setApril(8);
        limitSharingPercentsDTO2.setMay(8);
        limitSharingPercentsDTO2.setJune(8);
        limitSharingPercentsDTO2.setJuly(8);
        limitSharingPercentsDTO2.setAugust(8);
        limitSharingPercentsDTO2.setSeptember(8);
        limitSharingPercentsDTO2.setOctober(8);
        limitSharingPercentsDTO2.setNovember(8);
        limitSharingPercentsDTO2.setDecember(10);
        limitSharingPercentsDTO2.setLimitId(depLimit.getId());
        String limitSharingProcentsStr2 = objectMapper.writeValueAsString(limitSharingPercentsDTO2);
        
        String url = "/limitsharingprocents/" + getLimitSharingPercentsDTO.getId();
        
        //perform edit
        mockMvc.perform(put(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(limitSharingProcentsStr2))
               .andExpect(status().isOk());
        
        //perform get
        mockMvc.perform(get(url).with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.month0").value("8"))
               .andExpect(jsonPath("$.id").value(getLimitSharingPercentsDTO.getId().toString()));
        
        //perform delete
        mockMvc.perform(delete(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk());
        
        List<LimitSharingPercents> limitSharingPercentsList3 = limitSharingProcentsRepository.findAll();
        assertEquals(0, limitSharingPercentsList3.size());
    }
    
    @Test
    @DisplayName("Получение всех процентных распределений лимитов")
    @WithMockUser(username=USER1_ID, roles="GUEST")
    void test_getAll() throws Exception {
        var response = mockMvc.perform(
                                      get("/limitsharingprocents")
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                              .andExpect(status().isOk()).andReturn();
        List<GetLimitSharingPercentsDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(limitSharingProcentsRepository.findAll().size(), actual.size());
    }
}
