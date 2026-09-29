package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.CommonTest;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestAskTargets;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.limits.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.limits.dao.LimitRequestRepository;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.dto.DepLimitPrimaryDTO;
import ru.sberbank.ditsib.transport.limits.dto.DepLimitSharingDTO;
import ru.sberbank.ditsib.transport.limits.dto.DepLimitSharingPerTransportDTO;
import ru.sberbank.ditsib.transport.limits.dto.PrimarySharingDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.DepLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitRequestService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.database.limits.Tables.SERVICES;
import static ru.sber.transport.database.limits.Tables.TYPES;
import static ru.sberbank.ditsib.transport.limits.dto.v2.Month.JANUARY;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@AutoConfigureMockMvc
@Transactional
@DirtiesContext
@DisplayName("Проверка контроллера заявок на лимит подразделения версии 2")
@ActiveProfiles("test")
@SpringBootTest
class DepLimitRequestV2ControllerTest extends CommonTest {
    @Autowired
    private LimitRequestService limitRequestService;
    @Autowired
    private LimitRequestRepository limitRequestRepository;
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
    private DepLimitService depLimitService;
    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    int plannedYear = 0;

    @BeforeEach
    public void init() throws Exception {
        AuthorizeUtils.authorize(authorizationManager);
        dslContext.insertInto(SERVICES)
                .values("PASSENGER")
                .execute();
        dslContext.insertInto(TYPES)
                .values("TAXI")
                .execute();
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
                .firstName("vasya")
                .lastName("pupkin")
                .organizationId(organization1.getId())
                .departmentId(departmentChild1.getId())
                .build();

        testEmployee2 = Employee.builder()
                .id(userId2)
                .userId(userId2)
                .humanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2)
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

        departmentMain.setDepartmentHead(testEmployee2);
        departmentRepository.saveAndFlush(departmentMain);

        departmentChild1.setDepartmentHead(testEmployee1);
        departmentRepository.saveAndFlush(departmentChild1);

        departmentChild2.setDepartmentHead(testEmployee2);
        departmentRepository.saveAndFlush(departmentChild2);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR, 1);
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        plannedYear = plannedDate.getYear();

        var limitRequest = new LimitRequest();
        limitRequest.setLimitType(LimitType.DEPARTMENT);
        limitRequest.setAuthor(testEmployee1);
        limitRequest.setYear(plannedYear);
        limitRequest.setPeriod(Month.JANUARY);
        limitRequest.setSum(BigDecimal.valueOf(5));
        limitRequest.setTransportType(TransportTypeEnum.TAXI);
        limitRequest.setDescription("test");
        List<Approver> list = new ArrayList<>();
        Approver approver = new Approver();
        approver.setDepartment(departmentMain);
        approver.setEmployee(testEmployee2);
        approver.setLimitRequest(limitRequest);
        approver.setSum(BigDecimal.valueOf(5));
        list.add(approver);
        limitRequest.setApproverList(list);

        DepLimitPrimaryDTO limitDTO = new DepLimitPrimaryDTO();
        limitDTO.setYear(plannedYear);
        limitDTO.setSum(BigDecimal.valueOf(120));
        limitDTO.setLimitSharingType(LimitSharingType.MONTHLY);
        limitDTO.setParentId(null);
        limitDTO.setFinalSharing(false);
        limitDTO.setUseThisLimit(false);
        limitDTO.setLimitServiceType("PASSENGER");
        String depLimitStr = objectMapper.writeValueAsString(limitDTO);

        ResultActions result = mockMvc.perform(post("/deplimits/add/" + organization1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(depLimitStr))
                .andExpect(status().isOk());

        String contentAsString = result.andReturn().getResponse().getContentAsString();
        GetLimitDTO getLimitDTO = objectMapper.readValue(contentAsString, GetLimitDTO.class);
        assertEquals(120, getLimitDTO.getSum().intValue());
        UUID parentLimitId = getLimitDTO.getId();

        // распределить себе и на дочернее подразделение на такси
        PrimarySharingDTO limitSharingDTO1 =
                PrimarySharingDTO.builder().transportType(TransportTypeEnum.TAXI).sum(BigDecimal.valueOf(120)).build();
        List<PrimarySharingDTO> dtoList = new ArrayList<>();
        dtoList.add(limitSharingDTO1);
        String limitSharingStr = objectMapper.writeValueAsString(dtoList);

        mockMvc.perform(post("/deplimits/share_primary/" + parentLimitId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr))
                .andExpect(status().isOk());

        DepLimit limitMain = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentMain.getId(), plannedYear, "PASSENGER");
        assertThat(limitMain).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitMain.getLimitStatus());
        assertEquals(120, limitMain.getSum().intValue());
        assertEquals(0, limitMain.getReserve().intValue());

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
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr10))
                .andExpect(status().isOk());

        Limit limitChild1;

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.PLANNING, limitChild1.getLimitStatus());
        assertEquals(60, limitChild1.getSum().intValue());

        // share child limit
        String limitSharingStr11 = objectMapper.writeValueAsString(new ArrayList<>());
        mockMvc.perform(post("/deplimits/share_secondary/" + limitChild1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitSharingStr11))
                .andExpect(status().isOk());

        limitChild1 = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentChild1.getId(), plannedYear, "PASSENGER");
        assertThat(limitChild1).isNotNull();
        assertEquals(LimitStatus.SHARED, limitChild1.getLimitStatus());
        assertEquals(60, limitChild1.getSum().intValue());
    }

    @Test
    @DisplayName("CRUD заявок на лимит подразделения")
    void test_limitRequest_CRUD() throws Exception {
        List<LimitRequest> limitRequestList1 = limitRequestRepository.findAll();
        assertEquals(0, limitRequestList1.size());

        var limitRequestDTO = new DepLimitRequestV2DTO(
                plannedYear,
                JANUARY,
                TransportTypeEnum.TAXI,
                BigDecimal.valueOf(12),
                "test",
                LimitRequestAskTargets.PARENT,
                null
        );
        String limitRequestStr = objectMapper.writeValueAsString(limitRequestDTO);

        mockMvc.perform(post("/requests/department")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitRequestStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value("1200"))
                .andExpect(jsonPath("$.approverDtoList.length()").value("1"))
        ;

        List<LimitRequest> limitRequestList2 = limitRequestRepository.findAll();
        assertEquals(1, limitRequestList2.size());

        List<? extends LimitRequest> limitRequestAuthorList = limitRequestService.getByAuthor(testEmployee1.getId());
        assertEquals(1, limitRequestAuthorList.size());

        List<? extends LimitRequest> limitRequestApproverList = limitRequestService.getByApprover(testEmployee2.getId());
        assertEquals(1, limitRequestApproverList.size());

        var limitRequestDTO2 = new DepLimitRequestV2DTO(
                plannedYear,
                JANUARY,
                TransportTypeEnum.TAXI,
                BigDecimal.valueOf(24),
                "test",
                null,
                null
        );
        String limitRequestStr2 = objectMapper.writeValueAsString(limitRequestDTO2);

        var request = limitRequestService.getAll().getFirst();
        String url = "/requests/department/" + request.getId();

        //perform edit
        mockMvc.perform(put(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(limitRequestStr2))
                .andExpect(status().isOk());

        String urlGet = "/requests/" + request.getId();
        //perform get
        mockMvc.perform(get(urlGet)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value("2400"))
                .andExpect(jsonPath("$.id").value(request.getId().toString()));

        //perform delete
        limitRequestService.delete(limitRequestService.get(request.getId()).orElse(null));

        List<LimitRequest> limitRequestList3 = limitRequestRepository.findAll();
        assertEquals(0, limitRequestList3.size());
    }
}


