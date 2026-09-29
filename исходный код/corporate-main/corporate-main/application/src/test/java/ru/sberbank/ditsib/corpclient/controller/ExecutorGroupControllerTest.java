package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.CheckUserAccessService;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.ExecutorGroupRepository;
import ru.sberbank.ditsib.corpclient.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.ExecutorGroup;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.NewExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.mapper.ExecutorGroupMapper;
import ru.sberbank.ditsib.corpclient.service.FileService;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.corpclient.util.ContextHelper;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.jpa.show-sql=true"})
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера групп исполнителей")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
public class ExecutorGroupControllerTest extends SharedData {

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExecutorGroupMapper executorGroupMapper;

    @Autowired
    private ExecutorGroupRepository executorGroupRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private CheckUserAccessService checkAccessService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    private NewExecutorGroupDTO executorGroupDTO;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager em;

    @BeforeEach
    @Transactional
    void setupInitialisation() {
        AuthorizeUtils.authorize(roleCheckService);
        testOrganization1.setDigitId(1234L);
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setUserId(testEmployee1.getId());
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(this.testEmployee1);

        geoZoneRepository.save(testGeoZone1);

        assert testEmployee1.getId() != null;
        executorGroupDTO = NewExecutorGroupDTO.builder()
                .service("EMPLOYEE_TRANSPORTATION")
                .name("ОАО/Сбер Банк/Тест")
                .serviceLevel("level")
                .organizationId(testOrganization1.getId())
                .executors(List.of(testEmployee1.getId().toString()))
                .departments(List.of(testDepartment1.getId().toString()))
                .organizations(List.of(testOrganization1.getId().toString()))
                .customers(List.of(testEmployee1.getId().toString()))
                .geoZones(List.of(testGeoZone1.getId().toString()))
                .contractors(List.of(
                        contractorId1,
                        contractorId2,
                        contractorId3))
                .additionalFeature("test")
                .build();
    }

    @AfterEach
    void tearDownInitialisation() {
        transactionTemplate.executeWithoutResult(
                (s) -> {

                    em.createNativeQuery("delete from executor_group_executor").executeUpdate();
                    em.createNativeQuery("delete from executor_group_customer").executeUpdate();
                    em.createNativeQuery("delete from executor_group_geo_zone").executeUpdate();
                    em.createNativeQuery("delete from executor_group_organization").executeUpdate();
                    em.createNativeQuery("delete from executor_group").executeUpdate();
                    em.createNativeQuery("delete from employee_transport_type").executeUpdate();
                    em.createNativeQuery("delete from employee").executeUpdate();
                    em.createNativeQuery("delete from department").executeUpdate();
                    em.createNativeQuery("delete from position_taxi_classes").executeUpdate();
                    em.createNativeQuery("delete from position").executeUpdate();
                    em.createNativeQuery("delete from organization").executeUpdate();
                });
    }

    @SuppressWarnings("java:S5961")
    @Test
    @DisplayName("Добавление группы исполнителей")
    @Transactional
    void test_addExecutorGroup() throws Exception {
        doNothing().when(checkAccessService).check();

        var request = objectMapper.writeValueAsString(executorGroupDTO);

        var response =
                mockMvc.perform(
                                post(ExecutorGroupController.API_MAPPING)
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .content(request))
                        .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                ExecutorGroupDTO.class);

        var excepted = executorGroupRepository.findAll().getFirst();

        var message = executorGroupMapper.toMessage(executorGroupMapper.toDto(excepted));

        assertEquals(actual.getActive(), excepted.getActive());
        assertEquals(actual.getHumanReadableId(), excepted.getHumanReadableId());
        assertEquals(actual.getAuthorId(), excepted.getAuthorId());
        assertEquals(actual.getUserId(), excepted.getUserId());
        assertEquals(actual.getName(), excepted.getName());
        assertEquals(actual.getOrganizationId(), excepted.getOrganizationId());
        assertEquals(actual.getCreationTime(), excepted.getCreationTime());
        assertEquals(actual.getUpdatedAt(), excepted.getUpdateTime());
        assertEquals(actual.getCustomers().getFirst().getId(),
                excepted.getCustomers().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getDepartments().getFirst().getId(),
                excepted.getDepartments().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getGeoZones().getFirst().getId(),
                excepted.getGeoZones().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getOrganizations().getFirst().getId(),
                excepted.getOrganizations().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getExecutors().getFirst().getEmployeeId(),
                excepted.getExecutors().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getExecutors().getFirst().getDepartmentId(),
                excepted.getExecutors().stream().findFirst().orElseThrow().getDepartment().getId());

        assertEquals(excepted.getContractors().size(), actual.getContractors().size());
        Assertions.assertEquals(excepted.getContractors(),
                new HashSet<>(actual.getContractors()));
        assertEquals(actual.getAdditionalFeature(),
                excepted.getAdditionalFeature());

        assertEquals(actual.getId(), message.getId());
        assertEquals(actual.getActive(), message.getActive());
        assertEquals(actual.getHumanReadableId(), message.getHumanReadableId());
        assertEquals(actual.getAuthorId(), message.getAuthorId());
        assertEquals(actual.getUserId(), message.getUserId());
        assertEquals(actual.getName(), message.getName());
        assertEquals(actual.getOrganizationId(), message.getOrganizationId());
        assertEquals(actual.getCreationTime(), message.getCreationTime());
        assertEquals(actual.getUpdatedAt(), message.getUpdatedAt());
        assertEquals(actual.getCustomers().getFirst().getId(),
                message.getCustomers().getFirst().getId());
        assertEquals(actual.getDepartments().getFirst().getId(),
                message.getDepartments().getFirst().getId());
        assertEquals(actual.getGeoZones().getFirst().getId(),
                message.getGeoZones().getFirst().getId());
        assertEquals(actual.getOrganizations().getFirst().getId(),
                message.getOrganizations().getFirst().getId());
        assertEquals(actual.getExecutors().getFirst().getEmployeeId(),
                message.getExecutors().getFirst().getEmployeeId());
        assertEquals(actual.getExecutors().getFirst().getDepartmentId(),
                message.getExecutors().getFirst().getDepartmentId());
    }

    @Test
    @DisplayName("Добавление группы исполнителей с одинаковыми параметрами и разными сервисами")
    @Transactional
    void test_addExecutorGroupSameWithDifferentService() throws Exception {
        doNothing().when(checkAccessService).check();

        executorGroupDTO.setDepartments(null);
        executorGroupDTO.setCustomers(null);

        var request = objectMapper.writeValueAsString(executorGroupDTO);
        var baseService = executorGroupDTO.getService();
        var newService = executorGroupDTO.getService() + "test";

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        executorGroupDTO.setName("ОАО/Сбер Банк2/Тест");
        executorGroupDTO.setServiceLevel("VIP2");
        executorGroupDTO.setService(newService);
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        executorGroupDTO.setDepartments(List.of(testDepartment1.getId().toString()));
        executorGroupDTO.setService(baseService);
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        executorGroupDTO.setName("ОАО/Сбер Банк3/Тест");
        executorGroupDTO.setServiceLevel("VIP3");
        executorGroupDTO.setService(newService);
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        executorGroupDTO.setDepartments(List.of(testDepartment2.getId().toString()));
        executorGroupDTO.setService(baseService);
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        assert testEmployee1.getId() != null;
        executorGroupDTO.setName("ОАО/Сбер Банк4/Тест");
        executorGroupDTO.setServiceLevel("VIP4");
        executorGroupDTO.setCustomers(List.of(testEmployee1.getId().toString()));
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        executorGroupDTO.setName("ОАО/Сбер Банк5/Тест");
        executorGroupDTO.setServiceLevel("VIP5");
        executorGroupDTO.setService(newService);
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        assertEquals(7, executorGroupRepository.findAll().size());
    }

    @Test
    @DisplayName("Добавление группы исполнителей с неправильным именем")
    @Transactional
    void test_addExecutorGroupWithFailName() throws Exception {
        doNothing().when(checkAccessService).check();

        executorGroupDTO.setName("Fail");

        var request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Добавление группы исполнителей с существующим именем")
    @Transactional
    void test_addExecutorGroupWithNotUniqueName() throws Exception {
        doNothing().when(checkAccessService).check();

        var request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request));

        MvcResult response = mockMvc.perform(
                post(ExecutorGroupController.API_MAPPING)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(request))
                .andExpect(status().is4xxClientError()).andReturn();

        assertTrue(response.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("23011"));

        executorGroupDTO.setName("ОАО/Сбер-Банк/Тест");
        request = objectMapper.writeValueAsString(executorGroupDTO);

        response = mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().is4xxClientError()).andReturn();

        assertTrue(response.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("23014"));
    }

    @Test
    @DisplayName("Добавление группы исполнителей с ошибочной организацией")
    @Transactional
    void test_addExecutorGroupWithFailOrganisation() throws Exception {
        doNothing().when(checkAccessService).check();

        testOrganization2.setDigitId(1345L);
        testOrganization2 = organizationRepository.save(testOrganization2);

        executorGroupDTO.setOrganizationId(testOrganization2.getId());

        var request = objectMapper.writeValueAsString(executorGroupDTO);

        MvcResult response = mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().is4xxClientError()).andReturn();

        assertTrue(response.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("23001"));
    }

    @Test
    @DisplayName("Добавление группы исполнителей с такой же организацией/департаментом/сотрудникамиЗаказчиками")
    @Transactional
    void test_addExecutorGroupWithEqualsOrganisationDepartment() throws Exception {
        doNothing().when(checkAccessService).check();

        executorGroupDTO.setDepartments(null);
        executorGroupDTO.setCustomers(null);

        var request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        executorGroupDTO.setName("ОАО/Сбер Банк2/Тест");
        executorGroupDTO.setServiceLevel("VIP2");
        request = objectMapper.writeValueAsString(executorGroupDTO);

        MvcResult response = mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().is4xxClientError()).andReturn();
        assertTrue(response.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("23017"));

        executorGroupDTO.setDepartments(List.of(testDepartment1.getId().toString()));
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        executorGroupDTO.setName("ОАО/Сбер Банк3/Тест");
        executorGroupDTO.setServiceLevel("VIP3");
        request = objectMapper.writeValueAsString(executorGroupDTO);

        response = mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().is4xxClientError()).andReturn();
        assertTrue(response.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("23017"));

        executorGroupDTO.setDepartments(List.of(testDepartment2.getId().toString()));
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        assert testEmployee1.getId() != null;
        executorGroupDTO.setName("ОАО/Сбер Банк4/Тест");
        executorGroupDTO.setServiceLevel("VIP4");
        executorGroupDTO.setCustomers(List.of(testEmployee1.getId().toString()));
        request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        executorGroupDTO.setName("ОАО/Сбер Банк5/Тест");
        executorGroupDTO.setServiceLevel("VIP5");
        request = objectMapper.writeValueAsString(executorGroupDTO);

        response = mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().is4xxClientError()).andReturn();
        assertTrue(response.getResponse().getContentAsString(StandardCharsets.UTF_8).contains("23017"));

        assertEquals(4, executorGroupRepository.findAll().size());
    }

    @Test
    @DisplayName("Получение группы исполнителей")
    @Transactional
    void test_getExecutorGroup() throws Exception {
        test_addExecutorGroup();

        var excepted = executorGroupRepository.findAll().getFirst();

        var response =
                mockMvc.perform(
                                get(ExecutorGroupController.API_MAPPING + "/" + excepted.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        )
                        .andExpect(status().isOk()).andReturn();



        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                ExecutorGroupDTO.class);

        assertEquals(actual.getId(), excepted.getId());
        assertEquals(actual.getActive(), excepted.getActive());
        assertEquals(actual.getHumanReadableId(), excepted.getHumanReadableId());
        assertEquals(actual.getAuthorId(), excepted.getAuthorId());
        assertEquals(actual.getUserId(), excepted.getUserId());
        assertEquals(actual.getName(), excepted.getName());
        assertEquals(actual.getOrganizationId(), excepted.getOrganizationId());
        assertEquals(actual.getCreationTime(), excepted.getCreationTime());
        assertEquals(actual.getUpdatedAt(), excepted.getUpdateTime());
        assertEquals(actual.getCustomers().getFirst().getId(),
                excepted.getCustomers().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getDepartments().getFirst().getId(),
                excepted.getDepartments().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getGeoZones().getFirst().getId(),
                excepted.getGeoZones().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getOrganizations().getFirst().getId(),
                excepted.getOrganizations().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getExecutors().getFirst().getEmployeeId(),
                excepted.getExecutors().stream().findFirst().orElseThrow().getId());
        assertEquals(actual.getExecutors().getFirst().getDepartmentId(),
                excepted.getExecutors().stream().findFirst().orElseThrow().getDepartment().getId());
    }

    @Test
    @DisplayName("Получение группы исполнителей с ошибкой")
    @Transactional
    void test_getExecutorGroupWithException() throws Exception {
        test_addExecutorGroup();

        Exception resolvedException =
                mockMvc.perform(
                                get(ExecutorGroupController.API_MAPPING + "/" + USER2_ID)
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        )
                        .andExpect(status().isNotFound()).andReturn().getResolvedException();
        Assertions.assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Обновление группы исполнителей")
    @Transactional
    void test_updateExecutorGroup() throws Exception {
        doNothing().when(checkAccessService).check();

        var request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        var newExecutorGroup = executorGroupRepository.findAll().getFirst();

        geoZoneRepository.save(testGeoZone2);

        executorGroupDTO.setService("EMPLOYEE_TRANSPORTATION");
        executorGroupDTO.setName("ОАО/Сбер Банк/Обновление");
        executorGroupDTO.setGeoZones(List.of(testGeoZone2.getId().toString()));
        executorGroupDTO.setAdditionalFeature("newAdditionalFeature123");

        var updateRequest = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        put(ExecutorGroupController.API_MAPPING + "/" + newExecutorGroup.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(updateRequest))
                .andExpect(status().isOk());

        var excepted = executorGroupRepository.findAll().getFirst();

        var message = executorGroupMapper.toMessage(executorGroupMapper.toDto(excepted));

        assertEquals(newExecutorGroup.getId(), excepted.getId());
        assertEquals(USER1_ID, excepted.getAuthorId().toString());
        assertEquals(USER2_ID, excepted.getUserId().toString());
        assertEquals("ОАО/Сбер Банк/Обновление", excepted.getName());
        assertEquals("EMPLOYEE_TRANSPORTATION", excepted.getService());
        assertEquals(testGeoZone2.getId(),
                excepted.getGeoZones().stream().findFirst().orElseThrow().getId());
        assertEquals("newAdditionalFeature123",excepted.getAdditionalFeature());

        assertEquals(newExecutorGroup.getId(), message.getId());
        assertEquals(USER1_ID, message.getAuthorId().toString());
        assertEquals(USER2_ID, message.getUserId().toString());
        assertEquals("ОАО/Сбер Банк/Обновление", message.getName());
        assertEquals("EMPLOYEE_TRANSPORTATION", message.getService());
        assertEquals(testGeoZone2.getId(),
                message.getGeoZones().getFirst().getId());
    }

    @Test
    @DisplayName("Удаление группы исполнителей")
    @Transactional
    void test_deleteExecutorGroup() throws Exception {
        doNothing().when(checkAccessService).check();

        var request = objectMapper.writeValueAsString(executorGroupDTO);

        mockMvc.perform(
                        post(ExecutorGroupController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());

        var newExecutorGroup = executorGroupRepository.findAll().getFirst();

        geoZoneRepository.save(testGeoZone2);

        executorGroupDTO.setService("EMPLOYEE_TRANSPORTATION");
        executorGroupDTO.setName("ОАО/Сбер Банк/Обновление");
        executorGroupDTO.setGeoZones(List.of(testGeoZone2.getId().toString()));

        mockMvc.perform(
                        delete(ExecutorGroupController.API_MAPPING + "/" + newExecutorGroup.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        var excepted = executorGroupRepository.findAll().getFirst();

        var message = executorGroupMapper.toMessage(executorGroupMapper.toDto(excepted));

        assertEquals(newExecutorGroup.getId(), excepted.getId());
        assertEquals(USER1_ID, excepted.getAuthorId().toString());
        assertEquals(USER2_ID, excepted.getUserId().toString());
        assertEquals(false,excepted.getActive());

        assertEquals(newExecutorGroup.getId(), message.getId());
        assertEquals(USER1_ID, message.getAuthorId().toString());
        assertEquals(USER2_ID, message.getUserId().toString());
        assertEquals(false, message.getActive());
    }

    @DisplayName("Проверка получения списка сотрудников с пагинацией")
    @Test
    @Transactional
    void test_getAllPagination() throws Exception {
        doNothing().when(checkAccessService).check();

        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization = organizationRepository.save(organization);

        var department = new Department();
        department.setName("Department");
        department.setCode("Code");
        department.setOrganization(organization);
        department.setHumanReadableId("HRD");
        department.setUpdateTime(OffsetDateTime.now());
        department = departmentRepository.save(department);

        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        position = positionRepository.save(position);

        geoZoneRepository.save(testGeoZone1);

        var emps = new ArrayList<Employee>();

        for (var i = 0; i < 11; i++) {
            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setDepartment(department);
            employee.setLastName("LastName%02d".formatted(i));
            employee.setFirstName("FirstName%02d".formatted(i));
            employee.setPersonnelNumber("PersonnelNumber%02d".formatted(20 - i));
            employee.setPatronymic("Patronymic%02d".formatted(i));
            employee.setHumanReadableId("HumanReadable%02d".formatted(i));
            employee.setMobilePhone("Mobile%02d".formatted(i));
            employee.setEmail("Email%02d".formatted(i));
            employee.setPosition(position);
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());

            emps.add(employeeRepository.save(employee));
        }

        var egs = new ArrayList<ExecutorGroup>();

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            for (var i = 0; i < 10; i++) {
                var eg = ExecutorGroup.builder()
                        .id(UUID.randomUUID())
                        .departments(Set.of(department))
                        .organizations(Set.of(organization))
                        .executors(Set.of(emps.get(i + 1)))
                        .customers(Set.of(emps.get(i), emps.get(i + 1)))
                        .geoZones(Set.of(testGeoZone1))
                        .name("Name%02d".formatted(i))
                        .humanReadableId("IT-000%02d".formatted(10 - i))
                        .service(TransportServiceType.EMPLOYEE_TRANSPORTATION.name())
                        .active(true)
                        .organizationId(testOrganization1.getId())
                        .build();

                egs.add(executorGroupRepository.saveAndFlush(eg));
            }
        }

        var check = mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));

        var actual = check.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        for (var i = 0; i < 10; i++) {
            assertTrue(actual.contains("Name%02d".formatted(i)));
            assertTrue(actual.contains("IT-000%02d".formatted(10 - i)));
            assertTrue(actual.contains(TransportServiceType.EMPLOYEE_TRANSPORTATION.name()));
            assertTrue(actual.contains(egs.get(i).getId().toString()));
        }

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=1&size=5")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(1));
    }

    @DisplayName("Проверка получения списка сотрудников с пагинацией")
    @Test
    @Transactional
    void test_getWithFilter() throws Exception {
        doNothing().when(checkAccessService).check();

        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("masrn1");
        organization.setTid("tin1");
        organization = organizationRepository.save(organization);

        var organization1 = new Organization();
        organization1.setAddress("address1");
        organization1.setOfficialName("name1");
        organization1.setMsrn("masrn1");
        organization1.setTid("tin2");
        organization1 = organizationRepository.save(organization1);

        var department = new Department();
        department.setName("Department");
        department.setCode("Code");
        department.setOrganization(organization);
        department.setHumanReadableId("HRD");
        department.setUpdateTime(OffsetDateTime.now());
        department = departmentRepository.save(department);

        var department1 = new Department();
        department1.setName("Department1");
        department1.setCode("Code1");
        department1.setOrganization(organization1);
        department1.setHumanReadableId("HRD1");
        department1.setUpdateTime(OffsetDateTime.now());
        department1 = departmentRepository.save(department1);

        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        position = positionRepository.save(position);

        var position1 = new Position();
        position1.setName("Position1");
        position1.setOrganization(organization1);
        position1.setHumanReadableId("HRP1");
        position1 = positionRepository.save(position1);

        geoZoneRepository.save(testGeoZone1);

        geoZoneRepository.save(testGeoZone2);

        var emps = new ArrayList<Employee>();

        for (var i = 0; i < 11; i++) {
            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setDepartment(department);
            employee.setLastName("LastName%02d".formatted(i));
            employee.setFirstName("FirstName%02d".formatted(i));
            employee.setPersonnelNumber("PersonnelNumber%02d".formatted(20 - i));
            employee.setPatronymic("Patronymic%02d".formatted(i));
            employee.setHumanReadableId("HumanReadable%02d".formatted(i));
            employee.setMobilePhone("Mobile%02d".formatted(i));
            employee.setEmail("Email%02d".formatted(i));
            employee.setPosition(position);
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());

            emps.add(employeeRepository.save(employee));
        }

        var employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setNew(true);
        employee.setDepartment(department1);
        employee.setLastName("LastName%02d".formatted(30));
        employee.setFirstName("FirstName%02d".formatted(30));
        employee.setPersonnelNumber("PersonnelNumber%02d".formatted(30));
        employee.setPatronymic("Patronymic%02d".formatted(30));
        employee.setHumanReadableId("HumanReadable%02d".formatted(30));
        employee.setMobilePhone("Mobile%02d".formatted(30));
        employee.setEmail("Email%02d".formatted(30));
        employee.setPosition(position1);
        employee.setOrganization(department1.getOrganization());
        employee.setUpdateTime(OffsetDateTime.now());

        emps.add(employeeRepository.save(employee));

        var egs = new ArrayList<ExecutorGroup>();

        String cargoService = TransportServiceType.CARGO_TRANSPORTATION.name();

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);
            for (var i = 0; i < 10; i++) {
                var eg = ExecutorGroup.builder()
                        .id(UUID.randomUUID())
                        .departments(Set.of(department))
                        .organizations(Set.of(organization))
                        .executors(Set.of(emps.get(i + 1)))
                        .customers(Set.of(emps.get(i), emps.get(i + 1)))
                        .geoZones(Set.of(testGeoZone1))
                        .name("Name%02d".formatted(i))
                        .humanReadableId("IT-000%02d".formatted(10 - i))
                        .service(TransportServiceType.EMPLOYEE_TRANSPORTATION.name())
                        .active(true)
                        .organizationId(testOrganization1.getId())
                        .build();

                egs.add(executorGroupRepository.saveAndFlush(eg));

                eg.setId(UUID.randomUUID());
                eg.setActive(false);
                executorGroupRepository.saveAndFlush(eg);
            }
            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(organization1))
                    .departments(Set.of(department1))
                    .executors(Set.of(emps.get(9)))
                    .geoZones(Set.of(testGeoZone2))
                    .name("Сбер")
                    .humanReadableId("IT-0002")
                    .service(TransportServiceType.EMPLOYEE_TRANSPORTATION.name())
                    .active(true)
                    .organizationId(organization1.getId())
                    .build());

            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(organization1))
                    .departments(Set.of(department1))
                    .executors(Set.of(emps.get(9)))
                    .geoZones(Set.of(testGeoZone2))
                    .name("Сбер")
                    .humanReadableId("IT-0002")
                    .service(cargoService)
                    .active(true)
                    .organizationId(organization1.getId())
                    .build());
        }

        var check = mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&executorGroupName=ame01")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        String contentAsString = check.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(contentAsString.contains("Name01"));
        ExecutorGroup executorGroup = egs.stream().filter(eg -> eg.getName().equals("Name01")).findFirst().orElseThrow();
        assertTrue(contentAsString.contains(executorGroup.getHumanReadableId()));
        assertTrue(contentAsString.contains(executorGroup.getService()));
        assertTrue(contentAsString.contains(Objects.requireNonNull(executorGroup.getExecutors().stream().findFirst().orElseThrow().getId()).toString()));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&serviceType=" + cargoService)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&executorFIO=LastName01")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&executorFIO=LastName09")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&executorFIO=LastName&customerOrganizations=" + organization.getId() + "," + organization1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(11));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&customerDepartments=" + department1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&customerDepartments=" + department1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&executorGroupName=бе&customerDepartments=" + department1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&executorGroupName=би&customerDepartments=" + department1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&executorPersonnelNumber=" + emps.get(1).getPersonnelNumber())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&customerGeoZones=" + testGeoZone2.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20&executorOrganization=" + organization1.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        assertThat(executorGroupRepository.findByNameAndActiveTrue("NotExist").findFirst()).isNotPresent();
        assertThat(executorGroupRepository.findByNameAndActiveTrue("Сбер").findFirst()).isPresent();

        mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "?page=0&size=20")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(11));
    }

    @Test
    @DisplayName("Получение группы исполнителей по заказчику")
    void test_getExecutorGroupByCustomers() throws Exception {
        doNothing().when(checkAccessService).check();

        TestData testData = transactionTemplate.execute(s-> createTestData());

        var response = mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "/affiliation/" + testData.emps().getFirst().getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                ExecutorGroupDTO.class);

        assertEquals(actual.getOrganizations().getFirst().getId(), testData.organization().getId());
        assertEquals(actual.getDepartments().getFirst().getId(), testData.department().getId());
        assertEquals(actual.getCustomers().getFirst().getId(), testData.emps().getFirst().getId());
        assertEquals(actual.getExecutors().getFirst().getEmployeeId(), testData.emps().get(8).getId());

        response = mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "/affiliation/" + testData.emps().get(5).getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                ExecutorGroupDTO.class);

        assertEquals(actual.getOrganizations().getFirst().getId(), testData.organization().getId());
        assertEquals(actual.getDepartments().getFirst().getId(), testData.department().getId());
        assertEquals(actual.getExecutors().getFirst().getEmployeeId(), testData.emps().get(7).getId());

        response = mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "/affiliation/" + testData.employee().getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                ExecutorGroupDTO.class);

        assertEquals(actual.getOrganizations().getFirst().getId(), testData.organization1().getId());
        assertEquals(actual.getExecutors().getFirst().getEmployeeId(), testData.emps().get(9).getId());
    }

    @NotNull
    private TestData createTestData() {
        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("msrn");
        organization.setTid("tin");
        organization = organizationRepository.save(organization);

        var organization1 = new Organization();
        organization1.setAddress("address1");
        organization1.setOfficialName("name1");
        organization1.setMsrn("msrn1");
        organization1.setTid("tin1");
        organization1 = organizationRepository.save(organization1);

        var department = new Department();
        department.setName("Department");
        department.setCode("Code");
        department.setOrganization(organization);
        department.setHumanReadableId("HRD");
        department.setUpdateTime(OffsetDateTime.now());
        department = departmentRepository.save(department);

        var department1 = new Department();
        department1.setName("Department1");
        department1.setCode("Code1");
        department1.setOrganization(organization1);
        department1.setHumanReadableId("HRD1");
        department1.setUpdateTime(OffsetDateTime.now());
        department1 = departmentRepository.save(department1);

        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        position = positionRepository.save(position);

        var position1 = new Position();
        position1.setName("Position1");
        position1.setOrganization(organization1);
        position1.setHumanReadableId("HRP1");
        position1 = positionRepository.save(position1);

        geoZoneRepository.save(testGeoZone1);

        var emps = new ArrayList<Employee>();

        for (var i = 0; i < 11; i++) {
            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setDepartment(department);
            employee.setLastName("LastName%02d".formatted(i));
            employee.setFirstName("FirstName%02d".formatted(i));
            employee.setPersonnelNumber("PersonnelNumber%02d".formatted(20 - i));
            employee.setPatronymic("Patronymic%02d".formatted(i));
            employee.setHumanReadableId("HumanReadable%02d".formatted(i));
            employee.setMobilePhone("Mobile%02d".formatted(i));
            employee.setEmail("Email%02d".formatted(i));
            employee.setPosition(position);
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());

            emps.add(employeeRepository.save(employee));
        }

        var employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setNew(true);
        employee.setDepartment(department1);
        employee.setLastName("LastName%02d".formatted(30));
        employee.setFirstName("FirstName%02d".formatted(30));
        employee.setPersonnelNumber("PersonnelNumber%02d".formatted(30));
        employee.setPatronymic("Patronymic%02d".formatted(30));
        employee.setHumanReadableId("HumanReadable%02d".formatted(30));
        employee.setMobilePhone("Mobile%02d".formatted(30));
        employee.setEmail("Email%02d".formatted(30));
        employee.setPosition(position1);
        employee.setOrganization(department1.getOrganization());
        employee.setUpdateTime(OffsetDateTime.now());

        emps.add(employeeRepository.save(employee));

        try (MockedStatic<ContextHelper> mockedStatic = Mockito.mockStatic(ContextHelper.class)) {
            mockedStatic.when(ContextHelper::getCurrentUser).thenReturn(USER1_ID);

            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .departments(Set.of(department))
                    .organizations(Set.of(organization))
                    .executors(Set.of(emps.get(8)))
                    .customers(Set.of(emps.getFirst()))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name0")
                    .humanReadableId("IT-0000")
                    .service("EMPLOYEE_TRANSPORTATION")
                    .active(true)
                    .organizationId(organization.getId())
                    .build());

            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .departments(Set.of(department))
                    .organizations(Set.of(organization))
                    .executors(Set.of(emps.get(7)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name1")
                    .humanReadableId("IT-0001")
                    .service("EMPLOYEE_TRANSPORTATION")
                    .active(true)
                    .organizationId(organization.getId())
                    .build());

            executorGroupRepository.saveAndFlush(ExecutorGroup.builder()
                    .id(UUID.randomUUID())
                    .organizations(Set.of(organization1))
                    .executors(Set.of(emps.get(9)))
                    .geoZones(Set.of(testGeoZone1))
                    .name("Name2")
                    .humanReadableId("IT-0002")
                    .service("EMPLOYEE_TRANSPORTATION")
                    .active(true)
                    .organizationId(organization1.getId())
                    .build());
        }
        TestData testData = new TestData(organization, organization1, department, emps, employee);
        return testData;
    }

    private record TestData(Organization organization, Organization organization1, Department department, ArrayList<Employee> emps, Employee employee) {
    }

    @Test
    @DisplayName("Получение группы исполнителей по заказчику не принадлежащему ни одной группе исполнителей")
    @Transactional
    void test_getExecutorGroupByVoidCustomers() throws Exception {
        doNothing().when(checkAccessService).check();

        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("name");
        organization.setMsrn("masrn1");
        organization.setTid("tin1");
        organization = organizationRepository.save(organization);

        var department = new Department();
        department.setName("Department");
        department.setCode("Code");
        department.setOrganization(organization);
        department.setHumanReadableId("HRD");
        department.setUpdateTime(OffsetDateTime.now());
        department = departmentRepository.save(department);

        var position = new Position();
        position.setName("Position");
        position.setOrganization(organization);
        position.setHumanReadableId("HRP");
        position = positionRepository.save(position);

        geoZoneRepository.save(testGeoZone1);

        var employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setNew(true);
        employee.setDepartment(department);
        employee.setLastName("LastName%02d".formatted(30));
        employee.setFirstName("FirstName%02d".formatted(30));
        employee.setPersonnelNumber("PersonnelNumber%02d".formatted(30));
        employee.setPatronymic("Patronymic%02d".formatted(30));
        employee.setHumanReadableId("HumanReadable%02d".formatted(30));
        employee.setMobilePhone("Mobile%02d".formatted(30));
        employee.setEmail("Email%02d".formatted(30));
        employee.setPosition(position);
        employee.setOrganization(department.getOrganization());
        employee.setUpdateTime(OffsetDateTime.now());

        employeeRepository.save(employee);

        var response = mockMvc.perform(get(ExecutorGroupController.API_MAPPING + "/affiliation/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        assertFalse(StringUtils.hasText(response.getResponse().getContentAsString()));
    }
}
