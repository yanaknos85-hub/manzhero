package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.DelegateRecord;
import ru.sberbank.ditsib.corpclient.database.model.RecordStatus;
import ru.sberbank.ditsib.corpclient.dto.DelegateRecordDTO;
import ru.sberbank.ditsib.corpclient.dto.GetDelegateRecordDTO;
import ru.sberbank.ditsib.corpclient.exceptions.DataConstrainViolationException;
import ru.sberbank.ditsib.corpclient.service.DelegateService;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.corpclient.controller.DelegateController.getApiMappingByIds;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера делегатов")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
class DelegateControllerTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

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
    private DelegateRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DelegateService delegateService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EntityManager em;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    public void persistNeeded() {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testDepartment2.setOrganization(testOrganization2);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testDepartment3.setOrganization(testOrganization2);
        testDepartment3 = departmentRepository.save(testDepartment3);

        testPosition1.setHumanReadableId("PS-001-1");
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testPosition2.setHumanReadableId("PS-001-2");
        testPosition2.setOrganization(testOrganization1);
        testPosition2 = positionRepository.save(testPosition2);

        testPosition3.setHumanReadableId("PS-001-3");
        testPosition3.setOrganization(testOrganization2);
        testPosition3 = positionRepository.save(testPosition3);

        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testOrganization1);
        testEmployee1 = employeeRepository.saveAndFlush(testEmployee1);
        testEmployee1.setNew(false);

        testDepartment1.setHead(testEmployee1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee2.setDepartment(testDepartment1);
        testEmployee2.setPosition(testPosition2);
        testEmployee2.setOrganization(testOrganization1);
        testEmployee2 = employeeRepository.saveAndFlush(testEmployee2);
        testEmployee2.setNew(false);

        testEmployee3.setDepartment(testDepartment2);
        testEmployee3.setPosition(testPosition3);
        testEmployee3.setOrganization(testOrganization2);
        testEmployee3 = employeeRepository.saveAndFlush(testEmployee3);
        testEmployee3.setNew(false);

        testDepartment2.setHead(testEmployee3);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testEmployee4.setDepartment(testDepartment2);
        testEmployee4.setPosition(testPosition3);
        testEmployee4.setOrganization(testOrganization2);
        testEmployee4 = employeeRepository.saveAndFlush(testEmployee4);
        testEmployee4.setNew(false);

        testEmployee5.setDepartment(testDepartment3);
        testEmployee5.setPosition(testPosition3);
        testEmployee5.setOrganization(testOrganization2);
        testEmployee5 = employeeRepository.saveAndFlush(testEmployee5);
        testEmployee5.setNew(false);
    }


    @Test
    @DisplayName("Добавление записи о делегате")
    void test_addDelegateRecord_success() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        employeeRepository.save(testEmployee1);

        DelegateRecordDTO expected =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        var request = objectMapper.writeValueAsString(expected);

        var response =
                mockMvc.perform(
                                post(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .content(request))
                        .andExpect(status().isOk()).andReturn();
        GetDelegateRecordDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                GetDelegateRecordDTO.class);

        assertNotNull(actual.getId());
        assertEquals(expected.getSupervisorId(), actual.getSupervisorId());
        assertEquals(expected.getDelegateId(), actual.getDelegateId());
        assertNotNull(actual.getDelegateEmployee());
        assertNotNull(actual.getDelegateEmployee().firstName());
        assertEquals(expected.getDelegateId(), actual.getDelegateEmployee().id());
        assertEquals(expected.getStartDate(), actual.getStartDate());
        assertEquals(expected.getEndDate(), actual.getEndDate());
    }

    @Test
    @DisplayName("Изменение записи о делегате")
    void test_editDelegateRecord_success() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        employeeRepository.save(testEmployee1);

        DelegateRecordDTO original =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        GetDelegateRecordDTO expected =
                delegateService.add(testOrganization1.getId(), testDepartment1.getId(), original);

        expected.setEndDate(expected.getEndDate().plusYears(1));
        expected.setTransportType(TransportTypeEnum.PERSONAL);

        mockMvc.perform(
                        put(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()) + expected.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(objectMapper.writeValueAsString(expected)))
                .andExpect(status().isOk()).andReturn();
        GetDelegateRecordDTO actual =
                delegateService.get(testOrganization1.getId(), testDepartment1.getId(), expected.getId());

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getEndDate(), actual.getEndDate());
        assertEquals(expected.getTransportType(), actual.getTransportType());
        assertNotNull(actual.getDelegateEmployee());
        assertEquals(expected.getDelegateId(), actual.getDelegateEmployee().id());
    }

    @Test
    @DisplayName("Получение записи о делегате по идентификатору")
    void test_getDelegateRecord_success() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        employeeRepository.save(testEmployee1);

        DelegateRecordDTO original =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        GetDelegateRecordDTO expected =
                delegateService.add(testOrganization1.getId(), testDepartment1.getId(), original);

        var response =
                mockMvc.perform(
                                get(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()) + expected.getId())
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk()).andReturn();
        GetDelegateRecordDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                GetDelegateRecordDTO.class);

        assertNotNull(actual.getId());
        assertEquals(expected.getSupervisorId(), actual.getSupervisorId());
        assertEquals(expected.getDelegateId(), actual.getDelegateId());
        assertEquals(expected.getStartDate(), actual.getStartDate());
        assertEquals(expected.getEndDate(), actual.getEndDate());
        assertNotNull(actual.getDelegateEmployee());
        assertEquals(expected.getDelegateId(), actual.getDelegateEmployee().id());
    }

    @Test
    @DisplayName("Удаление записи о делегате")
    void test_deleteDelegateRecord_success() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        employeeRepository.save(testEmployee1);

        DelegateRecordDTO original =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        GetDelegateRecordDTO saved = delegateService.add(testOrganization1.getId(), testDepartment1.getId(), original);

        Pageable pageable = PageRequest.of(0, 10);

        assertEquals(1, delegateService.getAllBySupervisor(
                testOrganization1.getId(), testDepartment1.getId(), testEmployee1.getId(), pageable).getContent().size());

        mockMvc.perform(
                        delete(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()) + saved.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        assertEquals(0, delegateService.getAllBySupervisor(
                testOrganization1.getId(), testDepartment1.getId(), testEmployee1.getId(), pageable).getContent().size());


        //Проверка того, что запись есть в БД - но со статусом INACTIVE

        var rowCnt =
                (Long) em
                        .createNativeQuery("SELECT count(*) FROM corporate.delegate a WHERE a.status='INACTIVE' AND" +
                                " id='" + saved.getId() + "'")
                        .getSingleResult();
        assertEquals(1, rowCnt.intValue(), "Ожидалось, что в БД запись будет сохранена со статусом INACTIVE");
    }

    @Test
    @DisplayName("Удаление записи о делегате и последующая вставка на ту же дату")
    void test_deleteDelegateRecord_insert_success() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        employeeRepository.save(testEmployee1);

        DelegateRecordDTO original =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        GetDelegateRecordDTO saved = delegateService.add(testOrganization1.getId(), testDepartment1.getId(), original);

        Pageable pageable = PageRequest.of(0, 10);

        assertEquals(1, delegateService.getAllBySupervisor(
                testOrganization1.getId(), testDepartment1.getId(), testEmployee1.getId(), pageable).getContent().size());

        mockMvc.perform(
                        delete(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()) + saved.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        assertEquals(0, delegateService.getAllBySupervisor(
                testOrganization1.getId(), testDepartment1.getId(), testEmployee1.getId(), pageable).getContent().size());


        GetDelegateRecordDTO saved2 = delegateService.add(testOrganization1.getId(), testDepartment1.getId(), original);

        mockMvc.perform(
                        delete(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()) + saved2.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
    }


    @Test
    @DisplayName("Получение списка кандидатов в делегаты ")
    void test_getCandidates_success() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1 = positionRepository.saveAndFlush(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.saveAndFlush(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testOrganization1);
        employeeRepository.saveAndFlush(testEmployee1);

        DelegateRecordDTO original =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusYears(10))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), original);

        testEmployee3.setSupervisor(testEmployee1);
        testEmployee3.setDepartment(testDepartment1);
        testEmployee3.setPosition(testPosition1);
        testEmployee3.setOrganization(testOrganization1);
        employeeRepository.saveAndFlush(testEmployee3);
        testEmployee4.setSupervisor(testEmployee1);
        testEmployee4.setDepartment(testDepartment1);
        testEmployee4.setPosition(testPosition1);
        testEmployee4.setOrganization(testOrganization1);
        employeeRepository.saveAndFlush(testEmployee4);
        var response = mockMvc.perform(
                        get(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()) +
                                "candidates/" + testEmployee1.getId() + "/" + TransportTypeEnum.TAXI + "?date=" +
                                LocalDate.now().plusYears(5))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2)).andReturn().getResponse();

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Получение списка кандидатов в делегаты для вошедшего пользователя")
    void test_getMyCandidates_success() throws Exception {
        DelegateRecordDTO original =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusYears(10))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), original);

        testEmployee3.setSupervisor(testEmployee1);
        testEmployee3.setPosition(testEmployee1.getPosition());
        testEmployee3.setDepartment(testEmployee1.getDepartment());
        testEmployee3.setOrganization(testOrganization1);
        employeeRepository.saveAndFlush(testEmployee3);
        testEmployee4.setPosition(testEmployee1.getPosition());
        testEmployee4.setSupervisor(testEmployee1);
        testEmployee4.setDepartment(testDepartment1);
        testEmployee4.setOrganization(testOrganization1);
        employeeRepository.saveAndFlush(testEmployee4);
        mockMvc.perform(
                        get(
                                "/self/delegates/candidates/" + TransportTypeEnum.TAXI + "?date=" + LocalDate.now().plusYears(5))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Получение списка делегатов")
    void test_getDelegates_success() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testOrganization1);
        testEmployee1 = employeeRepository.saveAndFlush(testEmployee1);

        DelegateRecordDTO delegate =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.parse("2040-01-01"))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), delegate);
        testEmployee3.setSupervisor(testEmployee1);
        testEmployee3.setDepartment(testEmployee1.getDepartment());
        testEmployee3.setPosition(testEmployee1.getPosition());
        testEmployee3.setOrganization(testOrganization1);
        testEmployee3 = employeeRepository.saveAndFlush(testEmployee3);
        testEmployee4.setSupervisor(testEmployee1);
        testEmployee4.setDepartment(testDepartment1);
        testEmployee4.setOrganization(testOrganization1);
        testEmployee4.setPosition(testPosition1);
        testEmployee4 = employeeRepository.saveAndFlush(testEmployee4);
        delegate.setDelegateId(testEmployee3.getId());
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), delegate);
        delegate.setDelegateId(testEmployee4.getId());
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), delegate);

        var response = mockMvc.perform(
                        get(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()) +
                                "supervisors/" + testEmployee1.getId() + "?date=current&page=0&size=10")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();

        // Десериализуем ответ, получая поле `content` как список делегатов
        var jsonResponse = objectMapper.readTree(response.getResponse().getContentAsString(StandardCharsets.UTF_8));
        List<GetDelegateRecordDTO> searchResults = objectMapper.convertValue(
                jsonResponse.get("content"),
                new TypeReference<>() {}
        );

        assertEquals(3, searchResults.size());
    }

    @Test
    @DisplayName("Получение списка делегатов вошедшего пользователя")
    void test_getMyDelegates_success() throws Exception {
        DelegateRecordDTO delegate =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.parse("2040-01-01"))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), delegate);
        testEmployee3.setSupervisor(testEmployee1);
        testEmployee3.setDepartment(testEmployee1.getDepartment());
        testEmployee3.setPosition(testEmployee1.getPosition());
        testEmployee3.setOrganization(testEmployee1.getDepartment().getOrganization());
        employeeRepository.save(testEmployee3);
        testEmployee4.setSupervisor(testEmployee1);
        testEmployee4.setDepartment(testEmployee1.getDepartment());
        testEmployee4.setPosition(testEmployee1.getPosition());
        testEmployee4.setOrganization(testEmployee1.getDepartment().getOrganization());
        employeeRepository.saveAndFlush(testEmployee4);
        delegate.setDelegateId(testEmployee3.getId());
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), delegate);
        delegate.setDelegateId(testEmployee4.getId());
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), delegate);

        var response =
                mockMvc.perform(
                                get("/self/delegates?date=current")
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isOk()).andReturn();
        List<GetDelegateRecordDTO> searchResults =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });

        assertEquals(3, searchResults.size());
    }


    @Test
    @DisplayName("Получение списка записей делегирований по заданному пользователю")
    void test_getDelegateRecordsByEmpoyee_success() throws Exception {
        LocalDate endDate = LocalDate.parse("2040-01-01");
        testDepartment1.setHead(testEmployee2);
        departmentRepository.save(testDepartment1);
        testEmployee1.setOrganization(testOrganization1);
        testEmployee2.setOrganization(testOrganization2);
        testEmployee1.setSupervisor(testEmployee1);
        testEmployee1.setDepartment(testEmployee1.getDepartment());
        testEmployee1.setPosition(testEmployee1.getPosition());
        testEmployee1.setOrganization(testOrganization1);
        testEmployee2.setSupervisor(testEmployee1);
        testEmployee2.setDepartment(testEmployee1.getDepartment());
        testEmployee2.setPosition(testEmployee1.getPosition());
        testEmployee2.setOrganization(testOrganization1);
        employeeRepository.saveAndFlush(testEmployee2);
        employeeRepository.saveAndFlush(testEmployee1);

        DelegateRecordDTO delegate =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee2.getId())
                        .delegateId(testEmployee1.getId())
                        .startDate(LocalDate.now())
                        .endDate(endDate)
                        .transportType(TransportTypeEnum.TAXI)
                        .build();
        delegateService.add(testOrganization1.getId(), testDepartment1.getId(), delegate);
        testEmployee3.setSupervisor(testEmployee1);
        testEmployee3.setDepartment(testEmployee1.getDepartment());
        testEmployee3.setPosition(testEmployee1.getPosition());
        testEmployee3.setOrganization(testOrganization1);
        employeeRepository.save(testEmployee3);
        testEmployee4.setSupervisor(testEmployee1);
        testEmployee4.setDepartment(testDepartment1);
        testEmployee4.setPosition(testEmployee1.getPosition());
        testEmployee4.setOrganization(testOrganization1);
        employeeRepository.saveAndFlush(testEmployee4);
        delegate.setDelegateId(testEmployee3.getId());

        var response =
                mockMvc.perform(
                                get("/self/delegates/delegated?date=current")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk()).andReturn();
        List<GetDelegateRecordDTO> searchResults =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });

        assertEquals(1, searchResults.size());
        var result = searchResults.getFirst();
        assertEquals(testEmployee1.getId(), result.getDelegateId());
        assertEquals(testEmployee2.getId(), result.getSupervisorId());
        assertEquals(LocalDate.now(), result.getStartDate());
        assertEquals(endDate, result.getEndDate());

    }

    @Test
    @DisplayName("Добавление записи о делегате и замена статуса по контрольному сроку")
    void test_addDelegateRecord_check_deadline() throws DataConstrainViolationException {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        employeeRepository.save(testEmployee1);

        DelegateRecordDTO expected =
                DelegateRecordDTO.builder()
                        .supervisorId(testEmployee1.getId())
                        .delegateId(testEmployee2.getId())
                        .startDate(LocalDate.now().plusDays(1))
                        .endDate(LocalDate.now().plusDays(2))
                        .transportType(TransportTypeEnum.TAXI)
                        .build();

        GetDelegateRecordDTO actual = delegateService.add(testOrganization1.getId(), testDepartment1.getId(), expected);

        DelegateRecord delegateRecord = repository.findById(actual.getId()).orElseThrow(NoSuchElementException::new);
        delegateRecord.setStartDate(LocalDate.now().minusDays(2));
        delegateRecord.setEndDate(LocalDate.now().minusDays(1));
        repository.save(delegateRecord);

        assertNotNull(actual.getId());
        assertEquals(RecordStatus.ACTIVE, actual.getStatus());
        assertEquals(expected.getSupervisorId(), actual.getSupervisorId());
        assertEquals(expected.getDelegateId(), actual.getDelegateId());
        assertNotNull(actual.getDelegateEmployee());
        assertNotNull(actual.getDelegateEmployee().firstName());
        assertEquals(expected.getDelegateId(), actual.getDelegateEmployee().id());
        assertEquals(expected.getStartDate(), actual.getStartDate());
        assertEquals(expected.getEndDate(), actual.getEndDate());

        delegateService.updateDelegateStatusByDeadline();

        GetDelegateRecordDTO actual2 = delegateService.getById(actual.getId());
        assertEquals(RecordStatus.INACTIVE, actual2.getStatus());
    }

    @DisplayName("Добавление записи о делегате с датой раньше текущей")
    @Test
    void test_addDelegatedRecord_error() throws Exception {
        LocalDate from = LocalDate.now().minusMonths(2);
        LocalDate to = LocalDate.now().minusMonths(1);

        DelegateRecordDTO newDelegateRecordDTO =
                DelegateRecordDTO.builder().supervisorId(testEmployee1.getId()).delegateId(testEmployee2.getId())
                        .startDate(from)
                        .endDate(to)
                        .transportType(TransportTypeEnum.TAXI).build();

        var request = objectMapper.writeValueAsString(newDelegateRecordDTO);
        var result = mockMvc.perform(
                        post(DelegateController.getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId()))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.entity.name").value("DelegateRecordDTO"))
                .andExpect(jsonPath("$.problems.length()").value(2))
                .andExpect(jsonPath("$.problems[0].field").value("startDate"))
                .andExpect(jsonPath("$.problems[0].value").value(from.toString()))
                .andExpect(jsonPath("$.problems[1].field").value("endDate"))
                .andExpect(jsonPath("$.problems[1].value").value(to.toString()))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(0));

        assertThat(result).isNotNull();
    }
}