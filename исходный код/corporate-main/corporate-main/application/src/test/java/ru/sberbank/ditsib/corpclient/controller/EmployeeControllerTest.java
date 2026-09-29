package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.OrgStructureType;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;
import ru.sberbank.ditsib.corpclient.dto.EmployeeDTO;
import ru.sberbank.ditsib.corpclient.dto.EmployeeStatus;
import ru.sberbank.ditsib.corpclient.dto.NewEmployeeDTO;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера сотрудников")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class EmployeeControllerTest extends SharedData {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    @MockitoBean(name = "employeesOutput")
    private OutputBridge employeesOutput;

    @MockitoBean(name = "usersOutput")
    private OutputBridge usersOutput;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    public void persistNeeded() {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization2);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition2.setOrganization(testOrganization2);
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment2);
        testEmployee3.setDepartment(testDepartment2);
        testEmployee5.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testEmployee3.setOrganization(testDepartment2.getOrganization());
        testEmployee5.setOrganization(testDepartment1.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2 = employeeRepository.save(testEmployee2);
        testEmployee3 = employeeRepository.save(testEmployee3);
        testEmployee5 = employeeRepository.save(testEmployee5);
        testEmployee1.setNew(false);
        testEmployee2.setNew(false);
        testEmployee3.setNew(false);
        testEmployee5.setNew(false);
    }

    @AfterEach
    void afterEach() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    @DisplayName("Добавление сотрудника")
    void test_addEmployee() throws Exception {
        var value = new NewEmployeeDTO(
                testEmployee2.getFirstName(),
                testEmployee2.getLastName(),
                testEmployee2.getPatronymic(),
                "3422443214",
                testPosition2.getId(),
                testEmployee2.getMobilePhone(),
                testEmployee2.getEmail(),
                testEmployee1.getId(),
                Set.of("Role1", "Role2", "Role3"),
                Set.of(),
                null
        );
        var request = objectMapper.writeValueAsString(value);
        organizationRepository.flush();
        var response =
                mockMvc.perform(
                                post(EmployeeController.getApiMappingByOrgIdAndDepartmentId(testOrganization2.getId(),
                                        testDepartment2.getId()))
                                        .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(request))
                        .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                EmployeeDTO.class);

        var expected = employeeRepository.findById(actual.id()).orElse(null);

        assertNotNull(expected);
        assertEquals(expected.getId(), actual.id());
        assertEquals(expected.getPersonnelNumber(), actual.personnelNumber());
        assertEquals(testEmployee1.getId(), actual.supervisorId());
        assertEquals(testEmployee2.getDepartment().getId(), actual.departmentId());
        assertEquals(testEmployee2.getPosition().getId(), actual.positionId());
        assertEquals(testOrganization2.getId(), actual.organizationId());

        var actualMessage = getMessages(employeesOutput, EmployeeMessage.class);

        assertNotNull(actualMessage);
        assertEquals(expected.getId(), actualMessage.getId());
        assertEquals(expected.getPersonnelNumber(), actualMessage.getPersonnelNumber());
        assertEquals(testEmployee1.getId(), actualMessage.getSupervisorId());
        assertEquals(testEmployee2.getDepartment().getId(), actualMessage.getDepartmentId());
        assertEquals(testEmployee2.getPosition().getId(), actualMessage.getPositionId());
        assertEquals(testOrganization2.getId(), actualMessage.getOrganizationId());

        var userRoleMessage = getMessages(usersOutput, UserMessage.class, Map.of("type", OrgStructureType.EXTERNAL.name()));

        assertThat(userRoleMessage.getId()).isEqualTo(expected.getUserId());
        assertThat(userRoleMessage.roles())
                .hasSize(3)
                .contains("Role1")
                .contains("Role2")
                .contains("Role3");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    @DisplayName("Добавление сотрудника с номером телефона")
    void test_addEmployee_withPhoneNumber() throws Exception {
        testEmployee2.setMobilePhone("+79000000000");
        var value = new NewEmployeeDTO(
                testEmployee2.getFirstName(),
                testEmployee2.getLastName(),
                testEmployee2.getPatronymic(),
                "3422443214",
                testPosition2.getId(),
                testEmployee2.getMobilePhone(),
                testEmployee2.getEmail(),
                testEmployee1.getId(),
                Set.of("Role1", "Role2", "Role3"),
                Set.of(),
                ActiveStatus.ACTIVE
        );
        var request = objectMapper.writeValueAsString(value);
        var response =
                mockMvc.perform(
                                post(EmployeeController.getApiMappingByOrgIdAndDepartmentId(testOrganization2.getId(),
                                        testDepartment2.getId()))
                                        .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(request))
                        .andExpect(status().isOk()).andReturn();

        EmployeeDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                EmployeeDTO.class);

        Employee expected = employeeRepository.findById(actual.id()).orElse(null);

        assertNotNull(expected);
        assertEquals(expected.getId(), actual.id());
        assertEquals(expected.getPersonnelNumber(), actual.personnelNumber());
        assertEquals(testEmployee1.getId(), actual.supervisorId());
        assertEquals(testEmployee2.getDepartment().getId(), actual.departmentId());
        assertEquals(testEmployee2.getPosition().getId(), actual.positionId());
        assertEquals(testOrganization2.getId(), actual.organizationId());

        var actualMessage = getMessages(employeesOutput, EmployeeMessage.class);

        assertNotNull(actualMessage);
        assertEquals(expected.getId(), actualMessage.getId());
        assertEquals(expected.getPersonnelNumber(), actualMessage.getPersonnelNumber());
        assertEquals(testEmployee1.getId(), actualMessage.getSupervisorId());
        assertEquals(testEmployee2.getDepartment().getId(), actualMessage.getDepartmentId());
        assertEquals(testEmployee2.getPosition().getId(), actualMessage.getPositionId());
        assertEquals(testOrganization2.getId(), actualMessage.getOrganizationId());

        var userRoleMessage = getMessages(usersOutput, UserMessage.class, Map.of("type", OrgStructureType.EXTERNAL.name()));

        assertThat(userRoleMessage.getId()).isEqualTo(expected.getUserId());
        assertThat(userRoleMessage.roles())
                .hasSize(3)
                .contains("Role1")
                .contains("Role2")
                .contains("Role3");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    @DisplayName("Добавление сотрудника с корректировкрй номера телефона")
    void test_addEmployee_withPhoneNumber_correcting() throws Exception {
        testEmployee2.setMobilePhone("89000000000");
        var value = new NewEmployeeDTO(
                testEmployee2.getFirstName(),
                testEmployee2.getLastName(),
                testEmployee2.getPatronymic(),
                "3422443214",
                testPosition2.getId(),
                testEmployee2.getMobilePhone(),
                testEmployee2.getEmail(),
                testEmployee1.getId(),
                Set.of("Role1", "Role2", "Role3"),
                Set.of(),
                ActiveStatus.ACTIVE
        );
        var request = objectMapper.writeValueAsString(value);
        var response =
                mockMvc.perform(
                                post(EmployeeController.getApiMappingByOrgIdAndDepartmentId(testOrganization2.getId(),
                                        testDepartment2.getId()))
                                        .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(request))
                        .andExpect(status().isOk()).andReturn();

        EmployeeDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                EmployeeDTO.class);

        Employee expected = employeeRepository.findById(actual.id()).orElse(null);

        assertNotNull(expected);
        assertEquals(expected.getId(), actual.id());
        assertEquals(expected.getPersonnelNumber(), actual.personnelNumber());
        assertEquals(testEmployee1.getId(), actual.supervisorId());
        assertEquals(testEmployee2.getDepartment().getId(), actual.departmentId());
        assertEquals(testEmployee2.getPosition().getId(), actual.positionId());
        assertEquals(testEmployee2.getMobilePhone(), actual.mobilePhone().replace("+7", "8"));
        assertEquals(testOrganization2.getId(), actual.organizationId());

        var actualMessage = getMessages(employeesOutput, EmployeeMessage.class);

        assertNotNull(actualMessage);
        assertEquals(expected.getId(), actualMessage.getId());
        assertEquals(expected.getPersonnelNumber(), actualMessage.getPersonnelNumber());
        assertEquals(testEmployee1.getId(), actualMessage.getSupervisorId());
        assertEquals(testEmployee2.getDepartment().getId(), actualMessage.getDepartmentId());
        assertEquals(testEmployee2.getPosition().getId(), actualMessage.getPositionId());
        assertEquals(testOrganization2.getId(), actualMessage.getOrganizationId());

        var userRoleMessage = getMessages(usersOutput, UserMessage.class, Map.of("type", OrgStructureType.EXTERNAL.name()));

        assertThat(userRoleMessage.getId()).isEqualTo(expected.getUserId());
        assertThat(userRoleMessage.roles())
                .hasSize(3)
                .contains("Role1")
                .contains("Role2")
                .contains("Role3");
    }

    @Test
    @DisplayName("Добавление сотрудника с неверным телефоном")
    void test_addEmployee_withPWrongPhone() throws Exception {
        testEmployee2.setMobilePhone("39000000000");
        var value = new NewEmployeeDTO(
                testEmployee2.getFirstName(),
                testEmployee2.getLastName(),
                testEmployee2.getPatronymic(),
                "3422443214",
                testPosition2.getId(),
                testEmployee2.getMobilePhone(),
                testEmployee2.getEmail(),
                testEmployee1.getId(),
                Set.of("Role1", "Role2", "Role3"),
                Set.of(),
                ActiveStatus.ACTIVE
        );
        var request = objectMapper.writeValueAsString(value);
        var result = mockMvc.perform(
                        post(EmployeeController.getApiMappingByOrgIdAndDepartmentId(testOrganization2.getId(),
                                testDepartment2.getId()))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems.[0].field").value("mobilePhone"))
                .andExpect(jsonPath("$.problems.[0].value").value(testEmployee2.getMobilePhone()))
                .andExpect(jsonPath("$.problems.[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems.[0].constraints.[0].type").value("Pattern"))
                .andExpect(jsonPath("$.problems.[0].constraints.[0].value.pattern").value("((\\+7|8)\\d{10})|(^$)"));
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Добавление сотрудника с повторяющимися данными")
    void test_addDuplicate() throws Exception {
        String humanReadableId = "US-0019-1";
        Employee duplicateData = new Employee();
        duplicateData.setId(UUID.randomUUID());
        duplicateData.setUserId(duplicateData.getId());
        duplicateData.setFirstName(testEmployee1.getFirstName());
        duplicateData.setLastName(testEmployee1.getLastName());
        duplicateData.setPatronymic("aaa");
        duplicateData.setDepartment(testEmployee1.getDepartment());
        duplicateData.setPosition(testEmployee1.getPosition());
        duplicateData.setPersonnelNumber(testEmployee1.getPersonnelNumber());
        duplicateData.setEmail(testEmployee1.getEmail());
        duplicateData.setHumanReadableId(humanReadableId);

        var duplicate = new NewEmployeeDTO(
                duplicateData.getFirstName(),
                duplicateData.getLastName(),
                duplicateData.getPatronymic(),
                duplicateData.getPersonnelNumber(),
                duplicateData.getPosition().getId(),
                duplicateData.getMobilePhone(),
                duplicateData.getEmail(),
                null,
                Set.of(),
                Set.of(),
                ActiveStatus.ACTIVE
        );

        assert testEmployee1.getId() != null;
        var result = mockMvc.perform(post(EmployeeController.getApiMappingByOrgIdAndDepartmentId(testOrganization1.getId()
                        , testDepartment1.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Employee"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("personnelNumber"))
                .andExpect(jsonPath("$.problems[0].value").value(testEmployee1.getPersonnelNumber()))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(0))
                .andExpect(jsonPath("$.message").value(Matchers.startsWith("Conflict data on entity Employee.")));

        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Обновление сотрудника")
    void test_updateEmployee() throws Exception {
        testPosition1.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        testEmployee2.setPosition(testPosition2);
        testEmployee2.setSupervisor(null);
        testEmployee2 = employeeRepository.saveAndFlush(testEmployee2);
        testEmployee2.setNew(false);
        var saved =
                employeeService
                        .getEmployeeByOrganizationIdAndUserId(testOrganization2.getId(), testEmployee2.getUserId());
        var attribute = Attribute.builder().name(EMPLOYEE_ATTRIBUTES_2).build();
        attributeRepository.save(attribute);
        var attributes = new ArrayList<>(saved.attributes());
        attributes.add(AttributeDto.builder().id(attribute.getId()).name(EMPLOYEE_ATTRIBUTES_2).build());
        var toUpdate = new EmployeeDTO(
                saved.firstName(),
                saved.lastName(),
                saved.patronymic(),
                saved.personnelNumber(),
                testPosition1.getId(),
                saved.mobilePhone(),
                saved.email(),
                saved.supervisorId(),
                Set.of("NewRole1", "NewRole2", "NewRole3", "NewRole4"),
                saved.id(),
                saved.userId(),
                saved.humanReadableId(),
                saved.availableTransportTypes(),
                saved.personalCars(),
                saved.organizationId(),
                EmployeeStatus.ACTIVE,
                saved.gender(),
                saved.fireDate(),
                saved.externalEmail(),
                saved.room(),
                saved.consent(),
                new HashSet<>(attributes),
                saved.positionName(),
                saved.departmentId(),
                OrgStructureType.INTERNAL,
                saved.organizationName(),
                saved.departmentName()
        );

        assert testEmployee2.getId() != null;
        assertThat(employeeRepository.findById(testEmployee2.getId()).orElseThrow().getPosition().getId()).isEqualTo(testPosition2.getId());
        mockMvc.perform(put(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                        testOrganization2.getId(),
                        testDepartment2.getId()) +
                        testEmployee2.getId().toString()+ "/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(toUpdate)))
                .andExpect(status().isOk()).andReturn();

        var actualMessage = getMessages(employeesOutput, EmployeeMessage.class);

        List<Attribute> attributeAll = attributeRepository.findAll();
        assertEquals(1, attributeAll.size());
        assertThat(employeeRepository.findById(testEmployee2.getId()).orElseThrow().getPosition().getId()).isEqualTo(testPosition1.getId());

        assertThat(actualMessage.getSupervisorId()).isNull();

        var userRoleMessage = getMessages(usersOutput, UserMessage.class, Map.of("type", OrgStructureType.EXTERNAL.name()));

        assertThat(userRoleMessage.roles())
                .hasSize(4)
                .contains("NewRole1")
                .contains("NewRole2")
                .contains("NewRole3")
                .contains("NewRole4");
    }

    @Test
    @DisplayName("Обновление телефона сотрудника")
    void test_updateEmployee_phone() throws Exception {
        testEmployee2.setSupervisor(null);
        testEmployee2.setMobilePhone("79000000000");
        testEmployee2 = employeeRepository.saveAndFlush(testEmployee2);
        testEmployee2.setNew(false);

        var attribute = Attribute.builder().name(EMPLOYEE_ATTRIBUTES_2).build();
        attributeRepository.save(attribute);

        assert testEmployee2.getId() != null;
        var mobile = "79123456789";
        mockMvc.perform(patch("/self")
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                [
                                {
                                    "field": "mobilePhone",
                                    "value": "%s"
                                }
                                ]
                                """.formatted(mobile)))
                .andExpect(status().isOk()).andReturn();

        var saved = employeeRepository.getReferenceById(testEmployee2.getId());

        assertThat(saved.getMobilePhone()).isEqualTo(mobile);

        var actualMessage = getMessages(employeesOutput, EmployeeMessage.class);

        List<Attribute> attributeAll = attributeRepository.findAll();
        assertEquals(1, attributeAll.size());

        assertThat(actualMessage.getSupervisorId()).isNull();
        assertThat(actualMessage.getMobilePhone()).isEqualTo(mobile);
    }

    @Test
    @DisplayName("Обновление сотрудника повтоярющимися данными")
    void test_updateEmployeeDuplicate() throws Exception {
        testEmployee2.setSupervisor(null);
        employeeRepository.saveAndFlush(testEmployee2);
        var saved =
                employeeService
                        .getEmployeeByOrganizationIdAndUserId(testOrganization2.getId(), testEmployee2.getUserId());

        var toUpdate = new EmployeeDTO(
                saved.firstName(),
                saved.lastName(),
                saved.patronymic(),
                testEmployee3.getPersonnelNumber(),
                saved.positionId(),
                saved.mobilePhone(),
                saved.email(),
                saved.supervisorId(),
                saved.roles(),
                saved.id(),
                saved.userId(),
                saved.humanReadableId(),
                saved.availableTransportTypes(),
                saved.personalCars(),
                saved.organizationId(),
                saved.status(),
                saved.gender(),
                saved.fireDate(),
                saved.externalEmail(),
                saved.room(),
                saved.consent(),
                saved.attributes(),
                saved.positionName(),
                saved.departmentId(),
                saved.orgStructureType(),
                saved.organizationName(),
                saved.departmentName()
        );

        assert testEmployee2.getId() != null;
        assert testEmployee3.getId() != null;
        var result = mockMvc.perform(put(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                        testOrganization2.getId(), testDepartment2.getId()) +
                        testEmployee2.getId().toString()+ "/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(toUpdate)))
                .andExpect(status().isConflict()).andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Employee"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("personnelNumber"))
                .andExpect(jsonPath("$.problems[0].value").value(testEmployee3.getPersonnelNumber()))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(0))
                .andExpect(jsonPath("$.message").value(Matchers.startsWith("Conflict data on entity Employee.")));

        assertThat(result.andReturn().getResponse()).isNotNull();
    }


    @Test
    @DisplayName("Удаление сотрудника")
    void test_deleteEmployee() throws Exception {
        testEmployee1.getPersonalCars().clear();
        assertTrue(employeeRepository.findAll().size() > 0);

        assert testEmployee1.getId() != null;
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        assertEquals(ActiveStatus.ACTIVE, employeeRepository.findById(testEmployee1.getId()).get().getActiveStatus());

        mockMvc.perform(delete(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                        testOrganization1.getId(), testDepartment1.getId()) +
                        testEmployee1.getId().toString()+ "/")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();

        assertEquals(ActiveStatus.INACTIVE, employeeRepository.findById(testEmployee1.getId()).get().getActiveStatus());
        var actualMessage = getMessages(employeesOutput, EmployeeMessage.class);

        assertEquals(testEmployee1.getId(), actualMessage.getId());
        assertTrue(actualMessage.isDeleted());
    }

    @Test
    @DisplayName("Удаление несуществующего сотрудника")
    void test_deleteNonExistentEmployee() throws Exception {
        UUID rndUUID = UUID.randomUUID();
        Exception resolvedException = mockMvc.perform(
                        delete(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                                testOrganization1.getId(), testDepartment1.getId()) + rndUUID + "/")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(EntityNotFoundException.class, resolvedException.getClass());

    }

    @Test
    @DisplayName("Получение  существующего сотрудника")
    void test_getEmployee() throws Exception {
        assert testEmployee1.getId() != null;
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());

        var saved =
                mockMvc.perform(
                                get(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                                        testOrganization1.getId(), testDepartment1.getId())
                                        + testEmployee1.getId().toString())
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isOk()).andReturn();
        assertNotNull(saved);
        EmployeeDTO actual = objectMapper.readValue(saved.getResponse().getContentAsString(StandardCharsets.UTF_8),
                EmployeeDTO.class);


        assertEquals(testEmployee1.getId(), actual.id());
        assertEquals(testEmployee1.getPersonnelNumber(), actual.personnelNumber());
        assertEquals(testEmployee1.getEmail(), actual.email());
        assertEquals(testEmployee1.getDepartment().getId(), actual.departmentId());
    }

    @Test
    @DisplayName("Получение  существующего сотрудника по пользователю")
    void test_getEmployeeByUserId() throws Exception {
        var saved =
                mockMvc.perform(
                                get(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                                        testOrganization1.getId(), testDepartment1.getId())
                                        + "user/" + testEmployee1.getUserId())
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isOk()).andReturn();
        assertNotNull(saved);
        EmployeeDTO actual = objectMapper.readValue(saved.getResponse().getContentAsString(StandardCharsets.UTF_8),
                EmployeeDTO.class);


        assertEquals(testEmployee1.getId(), actual.id());
        assertEquals(testEmployee1.getPersonnelNumber(), actual.personnelNumber());
        assertEquals(testEmployee1.getEmail(), actual.email());
        assertEquals(testEmployee1.getDepartment().getId(), actual.departmentId());
    }

    @Test
    @DisplayName("Получение  несуществующего сотрудника по пользователю")
    void test_getNonExistentEmployeeUserId() throws Exception {
        UUID rndUUID = UUID.randomUUID();

        var resolvedException =
                mockMvc.perform(
                                get(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                                        testOrganization1.getId(), testDepartment1.getId())
                                        + "user/" + rndUUID)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isNotFound())
                        .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Получение  сотрудника по некорректному подразделению")
    void test_getEmployeeByNonExistentDepartment() throws Exception {
        assert testEmployee1.getId() != null;
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        UUID rndUUID = UUID.randomUUID();
        Exception resolvedException = mockMvc.perform(
                        get(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                                testOrganization1.getId(), rndUUID) + testEmployee1.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }


    @Test
    @DisplayName("Получение  несуществующего сотрудника ")
    void test_getNonExistentEmployee() throws Exception {
        String rndUUID = UUID.randomUUID().toString();
        Exception resolvedException = mockMvc.perform(
                        get(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                                testOrganization1.getId(), testDepartment1.getId()) + rndUUID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Получение списка сотрудников подразделения по подстроке ФИО")
    void test_getEmployeesBYFIO() throws Exception {
        testEmployee2.setDepartment(testDepartment1);
        testEmployee2.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setPosition(testPosition1);
        employeeRepository.save(testEmployee2);
        String searchSubstring = testEmployee1.getLastName() + " " + testEmployee1.getFirstName() +
                (testEmployee1.getPatronymic() == null ? "" :
                        " " + testEmployee1.getPatronymic());
        searchSubstring = searchSubstring.substring(1, searchSubstring.length() - 2);

        assert testEmployee1.getId() != null;
        var result = mockMvc.perform(
                        get(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                                testOrganization1.getId(), testDepartment1.getId()) + "search?fio=" +
                                searchSubstring)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testEmployee1.getId().toString()))
                .andExpect(jsonPath("$.content[0].userId").value(testEmployee1.getUserId().toString()))
                .andExpect(jsonPath("$.content[0].firstName").value(testEmployee1.getFirstName()))
                .andExpect(jsonPath("$.content[0].lastName").value(testEmployee1.getLastName()))
                .andExpect(jsonPath("$.content[0].patronymic").value(testEmployee1.getPatronymic()));

        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Получение списка сотрудников подразделения по подстроке ФИО - нет отчества")
    void test_getEmployeesBYFI() throws Exception {
        testEmployee5.setDepartment(testDepartment1);
        testEmployee5.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee5);
        String searchSubstring = testEmployee5.getLastName() + " " + testEmployee5.getFirstName() +
                (testEmployee5.getPatronymic() == null ? "" :
                        " " + testEmployee5.getPatronymic());
        searchSubstring = searchSubstring.substring(1, searchSubstring.length() - 2);

        assert testEmployee5.getId() != null;
        var result = mockMvc.perform(
                        get(EmployeeController.getApiMappingByOrgIdAndDepartmentId(
                                testOrganization1.getId(), testDepartment1.getId()) + "search?fio=" +
                                searchSubstring)
                                .with(jwt().jwt(builder -> builder.jti(USER5_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testEmployee5.getId().toString()))
                .andExpect(jsonPath("$.content[0].userId").value(testEmployee5.getUserId().toString()))
                .andExpect(jsonPath("$.content[0].firstName").value(testEmployee5.getFirstName()))
                .andExpect(jsonPath("$.content[0].lastName").value(testEmployee5.getLastName()));
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Тест подписания ПДн")
    void signPdnTest() throws Exception {
        assert testEmployee1.getId() != null;
        testEmployee1.setOrgStructureType(OrgStructureType.EXTERNAL);
        employeeRepository.save(testEmployee1);

        mockMvc.perform(
                        patch("/self/consent/")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        var employee = employeeRepository.findById(testEmployee1.getId()).orElseThrow();

        assertTrue(employee.isConsent());
    }

    @Test
    @DisplayName("Тест подписания ПДн внутренним сотрудником")
    void signPdnTest_internal() throws Exception {
        assert testEmployee1.getId() != null;
        testEmployee1.setOrgStructureType(OrgStructureType.INTERNAL);
        employeeRepository.save(testEmployee1);

        mockMvc.perform(
                        patch("/self/consent/")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        var employee = employeeRepository.findById(testEmployee1.getId()).orElseThrow();

        assertFalse(employee.isConsent());
    }
}
