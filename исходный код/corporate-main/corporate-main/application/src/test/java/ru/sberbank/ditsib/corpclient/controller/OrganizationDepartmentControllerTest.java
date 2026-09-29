package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.mapper.DepartmentMapper;
import ru.sberbank.ditsib.corpclient.mapper.OrganizationMapper;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера подразделений")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class OrganizationDepartmentControllerTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationDepartmentController organizationDepartmentController;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private PlatformTransactionManager manager;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean(name = "departmentsOutput")
    private OutputBridge departmentsOutput;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @AfterEach
    void afterEach() {
        positionRepository.findAll().forEach(p -> {
            p.setAvailableClasses(null);
            positionRepository.save(p);
        });
        employeeRepository.findAll().forEach(e -> {
            e.setAvailableTransportTypes(null);
            employeeRepository.save(e);
        });
        departmentRepository.findAll().forEach(d -> {
            d.setHead(null);
            departmentRepository.save(d);
        });
        employeeRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Добавление подразделения")
    void test_addDepartment() throws Exception {
        organizationRepository.saveAndFlush(testOrganization1);
        testOrganization1 = organizationRepository.findById(testOrganization1.getId()).get();

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee1.setNew(false);
        testEmployee1 = employeeRepository.save(testEmployee1);

        testDepartment2.setOrganization(testOrganization1);
        departmentRepository.save(testDepartment2);
        // сохраняю позицию
        positionRepository.save(testPosition1);
        testPosition1.setOrganization(testOrganization1);

        testEmployee1.getPersonalCars().clear();
        // сохраняю employee
        employeeRepository.save(testEmployee1);
        // устанавливаю департамент
        testEmployee1.setDepartment(testDepartment1);
        // добавляю его в департамент
        testDepartment1.addEmployee(testEmployee1);
        // делаю его главой подразделения
        testDepartment1.setHead(testEmployee1);
        testDepartment1.setParent(testDepartment2);
        testDepartment1 = departmentRepository.save(testDepartment1);

        assert testEmployee1.getId() != null;
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        assertEquals(testEmployee1.getDepartment().getId(), testDepartment1.getId());
        assertTrue(testDepartment1.getEmployees().contains(testEmployee1));

        NewDepartmentDTO newDepartmentDTO = new NewDepartmentDTO();
        newDepartmentDTO.setDepartmentHead(new DepartmentHeadDTO());
        newDepartmentDTO.getDepartmentHead().setId(testDepartment1.getHead().getId());
        newDepartmentDTO.setCode("67878967");
        newDepartmentDTO.setName("Test name");
        newDepartmentDTO.setGeozoneId(UUID.randomUUID());
        DepartmentParentDTO parentDTO = new DepartmentParentDTO();
        newDepartmentDTO.setParent(parentDTO);
        newDepartmentDTO.getParent().setId(testDepartment1.getId());
        var request = objectMapper.writeValueAsString(newDepartmentDTO);
        var response =
                mockMvc.perform(
                                post(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .content(request))
                        .andExpect(status().isOk()).andReturn();

        DepartmentDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                DepartmentDTO.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        var expected = departmentRepository.findById(actual.getId());
        assertTrue(expected.isPresent());

        assertEquals(expected.get().getName(), actual.getName());
        assertEquals(expected.get().getName(), actual.getName());
        assertEquals(expected.get().getLocation(), actual.getLocation());

        var actualMessage = getMessages(departmentsOutput, DepartmentMessage.class);
        assertNotNull(actualMessage);
        assertNotNull(actualMessage.getId());
        assertEquals(expected.get().getId(), actualMessage.getId());
        assertEquals(expected.get().getName(), actualMessage.getDepartmentName());
        assertEquals(expected.get().getName(), actualMessage.getDepartmentName());
        assertEquals(expected.get().getLocation(), actualMessage.getLocation());
        assertEquals(expected.get().getHumanReadableId(), actual.getHumanReadableId());
    }

    @Test
    @DisplayName("Добавление подразделения с повторяющимися данными")
    void test_addDuplicate() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testDepartment2.setOrganization(testOrganization1);
        testDepartment2.setParent(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testPosition1.setOrganization(testOrganization1);
        positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        var department = new NewDepartmentDTO();
        department.setName("NameNameName");
        department.setCode(testDepartment2.getCode());
        department.setLocation("Some location");
        DepartmentParentDTO parentDTO = new DepartmentParentDTO();
        department.setParent(parentDTO);
        department.getParent().setId(testDepartment2.getParent().getId());

        var request = objectMapper.writeValueAsString(department);
        var result = mockMvc.perform(
                        post(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Department"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("code"))
                .andExpect(jsonPath("$.problems[0].value").value(department.getCode()))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(0));

        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Добавление подразделения с повторяющимися относительно удаленного данными")
    void test_addDuplicateForDeleted() throws Exception {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee1.setNew(false);

        testDepartment2.setOrganization(testOrganization1);
        departmentRepository.save(testDepartment2);

        testDepartment1.setHead(testEmployee1);
        departmentRepository.save(testDepartment1);

        Department department = new Department();
        department.setOrganization(organizationMapper.shortDTOToOrganization(organizationMapper.organizationToShortDTO(testOrganization1)));
        department.setName("nnnmmm");
        department.setCode("798634");
        department.setLocation("Location");
        department.setHumanReadableId("658555");
        department.setUpdateTime(OffsetDateTime.now());
        departmentRepository.save(department);
        positionRepository.save(testPosition1);
        testPosition1.setOrganization(testOrganization1);

        testEmployee1.getPersonalCars().clear();
        employeeRepository.save(testEmployee1);
        testEmployee1.setDepartment(department);
        department.addEmployee(testEmployee1);
        department.setHead(testEmployee1);
        department.setParent(testDepartment2);

        NewDepartmentDTO newDepartmentDTO = new NewDepartmentDTO();

        newDepartmentDTO.setDepartmentHead(new DepartmentHeadDTO());
        newDepartmentDTO.getDepartmentHead().setId(department.getHead().getId());
        newDepartmentDTO.setCode("67878967");
        newDepartmentDTO.setName("Test name");
        DepartmentParentDTO parentDTO = new DepartmentParentDTO();
        newDepartmentDTO.setParent(parentDTO);
        newDepartmentDTO.getParent().setId(testDepartment1.getId());
        var request = objectMapper.writeValueAsString(newDepartmentDTO);
        departmentService.deleteDepartment(testDepartment1.getId());
        mockMvc.perform(
                post(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(request)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Обновление подразделения")
    void test_updateDepartment() throws Exception {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);
        testDepartment1.setHumanReadableId("DT-0001-1");
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        testEmployee1.getPersonalCars().clear();
        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);
        testDepartment1.setHead(testEmployee1);
        departmentRepository.save(testDepartment1);

        mockMvc.perform(get("/" + testOrganization1.getId() + "/departments/all")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(departmentRepository.count()))
                .andExpect(jsonPath("$.content[0].geozoneId").doesNotExist());

        var content = new AtomicReference<String>();
        var geoZoneId = new AtomicReference<UUID>();
        new TransactionTemplate(manager).executeWithoutResult(status -> {
            var toUpdate = departmentService.getDepartment(testDepartment1.getId());
            toUpdate.setHead(testDepartment1.getHead());
            toUpdate.setName(SharedData.DEPARTMENT2_NAME);
            toUpdate.setLocation(SharedData.DEPARTMENT2_LOCATION);
            toUpdate.setGeozone(UUID.randomUUID());
            geoZoneId.set(toUpdate.getGeozone());
            try {
                content.set(objectMapper.writeValueAsString(departmentMapper.departmentToDTO(toUpdate)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        mockMvc.perform(put(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId())
                        + testDepartment1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content.get())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        var actualMessage = getMessages(departmentsOutput, DepartmentMessage.class);

        assertEquals(testDepartment1.getId(), actualMessage.getId());
        assertEquals(SharedData.DEPARTMENT2_NAME, actualMessage.getDepartmentName());
        assertEquals(SharedData.DEPARTMENT2_LOCATION, actualMessage.getLocation());
        assertEquals("DT-0001-1", actualMessage.getHumanReadableId());

        assertEquals(geoZoneId.get(), departmentRepository.findAll().getFirst().getGeozone());

        mockMvc.perform(get("/" + testOrganization1.getId() + "/departments/all")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(departmentRepository.count()))
                .andExpect(jsonPath("$.content[0].geozoneId").value(geoZoneId.get().toString()));
    }

    @Test
    @DisplayName("Обновление подразделения. Зацикливание")
    @Transactional
    void test_updateDepartment_cycling() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testDepartment1.setHumanReadableId("DT-0001-1");
        testDepartment1.setName("name");
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testPosition1.setOrganization(testOrganization1);
        positionRepository.save(testPosition1);
        testEmployee1.getPersonalCars().clear();
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);
        testDepartment1.setHead(testEmployee1);
        testDepartment1.setParent(testDepartment1);
        departmentRepository.save(testDepartment1);
        var toUpdate = departmentService.getDepartment(testDepartment1.getId());
        toUpdate.setHead(testDepartment1.getHead());
        toUpdate.setName(SharedData.DEPARTMENT2_NAME);
        toUpdate.setLocation(SharedData.DEPARTMENT2_LOCATION);
        toUpdate.setParent(testDepartment1);
        String content = objectMapper.writeValueAsString(departmentMapper.departmentToDTONoLevel(toUpdate));
        var result = mockMvc.perform(put(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId())
                        + testDepartment1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Department"))
                .andExpect(jsonPath("$.entity.id").value(testDepartment1.getId().toString()))
                .andExpect(jsonPath("$.message").value("INFINITE_LOOP"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("parentId"))
                .andExpect(jsonPath("$.problems[0].value").value(testDepartment1.getId().toString()));
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Обновление подразделения. Зацикливание, через нескольких")
    @Transactional
    void test_updateDepartment_cycling_nonDirect() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);

        testDepartment1.setHumanReadableId("DT-0001-1");
        testDepartment1.setName("name");
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1.setHead(null);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testDepartment2.setOrganization(testOrganization2);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testDepartment1.setParent(testDepartment2);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testDepartment3.setParent(testDepartment1);
        testDepartment3.setOrganization(testOrganization2);
        testDepartment3 = departmentRepository.save(testDepartment3);

        testDepartment2.setParent(testDepartment3);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testPosition1.setOrganization(testOrganization1);
        positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.getPersonalCars().clear();
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);

        testDepartment1.setHead(testEmployee1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        var toUpdate = departmentService.getDepartment(testDepartment1.getId());
        toUpdate.setHead(testDepartment1.getHead());
        toUpdate.setName(SharedData.DEPARTMENT2_NAME);
        toUpdate.setLocation(SharedData.DEPARTMENT2_LOCATION);
        toUpdate.setParent(testDepartment1);
        String content = objectMapper.writeValueAsString(departmentMapper.departmentToDTONoLevel(toUpdate));
        var result = mockMvc.perform(put(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId())
                        + testDepartment1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Department"))
                .andExpect(jsonPath("$.entity.id").value(testDepartment1.getId().toString()))
                .andExpect(jsonPath("$.message").value("INFINITE_LOOP"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("parentId"))
                .andExpect(jsonPath("$.problems[0].value").value(testDepartment1.getParent().getId().toString()));
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Добавление подразделения с несуществующим родительским подразделением")
    void test_addFakeParentDepartments() throws Exception {
        Organization found_testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);
        testDepartment1.setOrganization(found_testOrganization1);
        departmentRepository.save(testDepartment1);

        testDepartment2.setOrganization(found_testOrganization1);
        departmentRepository.save(testDepartment2);
        testPosition1.setOrganization(found_testOrganization1);
        positionRepository.save(testPosition1);

        testEmployee1.getPersonalCars().clear();
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);
        testEmployee1.setDepartment(testDepartment1);
        testDepartment1.addEmployee(testEmployee1);
        testDepartment1.setHead(testEmployee1);
        testDepartment1.setParent(testDepartment2);

        assert testEmployee1.getId() != null;
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        assertEquals(testEmployee1.getDepartment().getId(), testDepartment1.getId());
        assertTrue(testDepartment1.getEmployees().contains(testEmployee1));

        NewDepartmentDTO newDepartmentDTO = new NewDepartmentDTO();

        newDepartmentDTO.setDepartmentHead(new DepartmentHeadDTO());
        newDepartmentDTO.getDepartmentHead().setId(testDepartment1.getHead().getId());
        DepartmentParentDTO parentDTO = new DepartmentParentDTO();
        newDepartmentDTO.setParent(parentDTO);
        newDepartmentDTO.getParent().setId(UUID.randomUUID());
        newDepartmentDTO.setCode("67878967");
        newDepartmentDTO.setName("Test name");
        var request = objectMapper.writeValueAsString(newDepartmentDTO);

        Exception resolvedException = mockMvc.perform(
                        post(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()))
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
        assertNotNull(newDepartmentDTO.getDepartmentHead().getId());
    }

    @Test
    @DisplayName("Добавление подразделения с несуществующим главой подразделения")
    void test_addFakeDepartmentHead() throws Exception {
        Organization found_testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);
        testDepartment1.setOrganization(found_testOrganization1);
        departmentRepository.save(testDepartment1);

        testDepartment2.setOrganization(found_testOrganization1);
        departmentRepository.save(testDepartment2);
        testPosition1.setOrganization(found_testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        testPosition1.setOrganization(found_testOrganization1);

        testEmployee1.getPersonalCars().clear();
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testDepartment1.addEmployee(testEmployee1);
        testDepartment1.setHead(testEmployee1);
        testDepartment1.setParent(testDepartment2);

        assert testEmployee1.getId() != null;
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        assertEquals(testEmployee1.getDepartment().getId(), testDepartment1.getId());
        assertTrue(testDepartment1.getEmployees().contains(testEmployee1));

        NewDepartmentDTO newDepartmentDTO = new NewDepartmentDTO();

        newDepartmentDTO.setDepartmentHead(new DepartmentHeadDTO());
        newDepartmentDTO.getDepartmentHead().setId(UUID.randomUUID());
        newDepartmentDTO.setCode("67878967");
        newDepartmentDTO.setName("Test name");
        DepartmentParentDTO parentDTO = new DepartmentParentDTO();
        newDepartmentDTO.setParent(parentDTO);
        newDepartmentDTO.getParent().setId(testDepartment1.getId());
        var request = objectMapper.writeValueAsString(newDepartmentDTO);

        Exception resolvedException = mockMvc.perform(
                        post(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()))
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Обновление подразделения повтоярющимися данными")
    void test_updateDuplicate() throws Exception {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);
        testDepartment1.setOrganization(testOrganization1);
        departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        positionRepository.save(testPosition1);
        testEmployee1.getPersonalCars().clear();
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);
        testDepartment1.setName("name");
        testDepartment1.setHead(testEmployee1);

        testDepartment2.setOrganization(testOrganization1);
        departmentRepository.save(testDepartment2);
        departmentRepository.flush();

        DepartmentDTO newValue = departmentMapper.departmentToDTO(testDepartment2);
        newValue.setCode(testDepartment2.getCode());
        newValue.setFullStructurePath("FSC");

        newValue.setDepartmentHead(new DepartmentHeadDTO());
        newValue.getDepartmentHead().setId(testDepartment1.getHead().getId());
        var request = objectMapper.writeValueAsString(newValue);
        var result = mockMvc.perform(
                        put(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId())
                                + testDepartment1.getId().toString())
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Department"))
                .andExpect(jsonPath("$.problems.length()").value(2));

        assertThat(result.andReturn().getResponse()).isNotNull();

    }

    @Test
    @DisplayName("Удаление подразделения")
    void test_deleteDepartment() throws Exception {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);

        testDepartment1.setOrganization(testOrganization1);
        var department = departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        var initialCount = departmentRepository.count();

        mockMvc.perform(delete(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId())
                        + testDepartment1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        assertEquals(initialCount, departmentRepository.count());
        assertEquals(ActiveStatus.INACTIVE, departmentRepository.findById(department.getId()).get().getActiveStatus());

        var actualMessage = getMessages(departmentsOutput, DepartmentMessage.class);

        assertEquals(testDepartment1.getId(), actualMessage.getId());
        assertTrue(actualMessage.isDeleted());
    }

    @Test
    @DisplayName("Удаление несуществующего подразделения")
    void test_deleteNonExistentDepartment() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        UUID rndUUID = UUID.randomUUID();
        Exception resolvedException = mockMvc.perform(
                        delete(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()) +
                                rndUUID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(EntityNotFoundException.class, resolvedException.getClass());

    }

    @Test
    @DisplayName("Получение  существующего подразделения")
    void test_getDepartment() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        assertTrue(departmentRepository.findById(testDepartment1.getId()).isPresent());

        var saved =
                mockMvc.perform(
                                get(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId())
                                        + testDepartment1.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk()).andReturn();
        assertNotNull(saved);
        DepartmentDTO actual = objectMapper.readValue(saved.getResponse().getContentAsString(StandardCharsets.UTF_8),
                DepartmentDTO.class);


        assertEquals(testDepartment1.getId(), actual.getId());
        assertEquals(testDepartment1.getName(), actual.getName());
        assertEquals(testDepartment1.getLocation(), actual.getLocation());
    }

    @Test
    @DisplayName("Получение  отсутствующего подразделения")
    void test_getAbsentDepartment() throws Exception {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        String rndUUID = UUID.randomUUID().toString();
        Exception resolvedException = mockMvc.perform(
                        get(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId())
                                + rndUUID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Получение списка активных подразделений организации")
    void test_getActiveDepartments() throws Exception {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testPosition1.setOrganization(testOrganization1);
        positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        departmentService.deleteDepartment(testDepartment2.getId());

        mockMvc.perform(
                        get(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(testDepartment1.getId().toString()))
        ;
        assertEquals(2, departmentRepository.findAll().size());
    }

    @Test
    @DisplayName("Получение списка активных подразделений организации по списку их идентификаторов")
    void test_getActiveDepartmentsBySetId() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testPosition1.setOrganization(testOrganization1);
        positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        departmentService.deleteDepartment(testDepartment1.getId());

        var request = objectMapper.writeValueAsString(Set.of(testDepartment1.getId(), testDepartment2.getId()));

        mockMvc.perform(
                        post(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()) + "departments_search")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(testDepartment2.getId().toString()));

        assertEquals(2, departmentRepository.findAll().size());
    }

    @Test
    @DisplayName("Получение полного списка подразделений организации ")
    void test_getAllDepartments() throws Exception {
        organizationRepository.save(testOrganization1);
        testOrganization1 = organizationRepository.findById(testOrganization1.getId()).get();

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        testDepartment2.setOrganization(testOrganization1);
        testDepartment2 = departmentRepository.save(testDepartment2);

        departmentService.deleteDepartment(testDepartment2.getId());

        mockMvc.perform(
                        get(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()) + "all")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(testDepartment1.getId().toString()))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[1].id").value(testDepartment2.getId().toString()))
                .andExpect(jsonPath("$.content[1].status").value("INACTIVE"))
        ;

        assertEquals(2, departmentRepository.findAll().size());
    }

    @Test
    @DisplayName("Редактирование подразделения - проверим humanReadableId")
    void test_editDepartment() {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);

        testDepartment1.setHumanReadableId("DT-0001-1");
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        DepartmentDTO toUpdate = new DepartmentDTO();
        toUpdate.setId(testDepartment1.getId());
        toUpdate.setCode("10");
        toUpdate.setOrganizationId(testOrganization1.getId());
        toUpdate.setChildren(null);
        toUpdate.setEmployees(null);
        toUpdate.setName(SharedData.DEPARTMENT2_NAME);
        toUpdate.setLocation(SharedData.DEPARTMENT2_LOCATION);

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("typ", "JWT").jti(USER1_ID).build()));

        organizationDepartmentController.editDepartment(testOrganization1.getId(), testDepartment1.getId(), toUpdate);

        var actualMessage = getMessages(departmentsOutput, DepartmentMessage.class);

        assertEquals(testDepartment1.getId(), actualMessage.getId());
        assertEquals(SharedData.DEPARTMENT2_NAME, actualMessage.getDepartmentName());
        assertEquals(SharedData.DEPARTMENT2_LOCATION, actualMessage.getLocation());
        assertEquals("DT-0001-1", actualMessage.getHumanReadableId());
    }


    @Test
    @DisplayName("Получение дочерних подразделений")
    void test_getChildrenDepartments() throws Exception {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);
        testDepartment1.setHumanReadableId("DT-0001-1");
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment3.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2.setParent(testDepartment1);
        departmentRepository.save(testDepartment2);
        testDepartment3.setParent(testDepartment1);
        departmentRepository.save(testDepartment3);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        var childrenDepartments = Map.of(testDepartment2.getId(), testDepartment2,
                testDepartment3.getId(), testDepartment3);


        var result = mockMvc.perform(
                        get(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()) + testDepartment1.getId() + "/children")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
        var expectedList = new ArrayList<>(childrenDepartments.values());
        expectedList.sort(Comparator.comparing(Department::getName));
        for (var i = 0; i < 2; i++) {
            var departmentShortDto = expectedList.get(i);

            result
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(departmentShortDto.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].departmentName".formatted(i)).value(departmentShortDto.getName()))
            ;
        }
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Тест признака филиала")
    void test_checkFilialFlagDepartments() {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);
        testDepartment1.setHumanReadableId("DT-0001-1");
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment3.setOrganization(testOrganization1);
        testDepartment4.setOrganization(testOrganization1);
        testDepartment5.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2.setParent(testDepartment1);
        testDepartment2.setFilialFlag(true);
        departmentRepository.save(testDepartment2);
        testDepartment3.setParent(testDepartment1);
        testDepartment3.setFilialFlag(true);
        departmentRepository.save(testDepartment3);
        testDepartment4.setParent(testDepartment2);
        departmentRepository.save(testDepartment4);
        testDepartment5.setParent(testDepartment3);
        testDepartment5.setFilialFlag(true);
        departmentRepository.save(testDepartment5);

        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        CheckFilialFlagResutlDTO checkFilialFlagResutlDTO = departmentService.checkFilialFlagAll(testOrganization1.getId());
        assertThat(checkFilialFlagResutlDTO.getNumOfFilialFlags()).isEqualTo(3);
        assertThat(checkFilialFlagResutlDTO.getNumOfDeletedFlags()).isEqualTo(1);
    }

    @Test
    @DisplayName("Тест изменения флага филиала")
    void test_checkPatchFilialFlag() throws Exception {
        testOrganization1 = organizationRepository.saveAndFlush(testOrganization1);
        testDepartment1.setHumanReadableId("DT-0001-1");
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        var department1a = departmentRepository.findById(testDepartment1.getId());
        assertTrue(department1a.isPresent());
        assertThat(department1a.get().getFilialFlag()).isFalse();

        mockMvc.perform(patch(OrganizationDepartmentController.getApiMappingByOrgId(testOrganization1.getId()) + testDepartment1.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content("""
                                [
                                {
                                    "field": "filialFlag",
                                    "value": "true"
                                }
                                ]
                                """))
                .andExpect(status().isOk()).andReturn();

        var department1b = departmentRepository.findById(testDepartment1.getId());
        assertTrue(department1b.isPresent());
        assertThat(department1b.get().getFilialFlag()).isTrue();

    }
}
