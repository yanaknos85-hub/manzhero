package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.AttributeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.purpose.*;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripPurposeType;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера целей поездки")
@Transactional
@ActiveProfiles("test")
class TripPurposeControllerTest extends SharedData {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TripPurposeRepository tripPurposeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private JpaRepository<Department, UUID> departmentRepository;

    @Autowired
    private JpaRepository<Employee, UUID> employeeRepository;

    @Autowired
    private JpaRepository<Position, UUID> positionRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean(name = "purposeOutput")
    private OutputBridge purposeOutput;

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
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment2);
        testEmployee5.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testEmployee5.setOrganization(testDepartment1.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2 = employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee5);
    }

    @Test
    @DisplayName("Добавление")
    void addTripPurpose() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder().label(label).build();

        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        var response = mockMvc.perform(
                        post(Strings.concat("/", organization.getId(), "/purposes")).contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                TripPurposeDTO.class);

        Optional<TripPurpose> tripPurpose =
                tripPurposeRepository.findByLabelAndOrganization(label, organization.getId());

        assertTrue(tripPurpose.isPresent());
        assertEquals(actual.getLabel(), tripPurpose.get().getLabel());
        assertEquals(actual.getId(), tripPurpose.get().getId());
        assertEquals(TripPurposeType.CORPORATE, actual.getPurposeType());

        var message = getMessages(purposeOutput, TripPurposeMessage.class);
        assertEquals(message.getLabel(), tripPurpose.get().getLabel());
        assertEquals(message.getId(), tripPurpose.get().getId());
        assertEquals(message.getOrganization(), organization.getId());
    }

    @Test
    @DisplayName("Добавление с днем недели")
    void addTripPurposeAndWeekDay() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        TripPurposeWeekdayDTO tripPurposeWeekdayDTO = new TripPurposeWeekdayDTO();
        tripPurposeWeekdayDTO.setWeekday(DayOfWeek.MONDAY);

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder()
                .label(label)
                .tripPurposeWeekday(tripPurposeWeekdayDTO).build();


        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        var response = mockMvc.perform(
                        post(Strings.concat("/", organization.getId(), "/purposes"))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<LinkedHashMap<String, Object>>() {
                });

        assertNotNull(actual.get("tripPurposeWeekdays"));
    }

    @Test
    @DisplayName("Добавление с периодом дат")
    void addTripPurposeAndDatePeriod() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        TripPurposeDateDTO tripPurposeDateDTO = new TripPurposeDateDTO();
        tripPurposeDateDTO.setStartDate(LocalDateTime.now());
        tripPurposeDateDTO.setEndDate(LocalDateTime.now().plusDays(2));

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder().label(label)
                .tripPurposeDate(tripPurposeDateDTO)
                .build();

        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        var response = mockMvc.perform(
                        post(Strings.concat("/", organization.getId(), "/purposes")).contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                NewTripPurposeDTO.class);

        assertNotNull(actual.getTripPurposeDates());
        assertEquals(0, actual.getTripPurposeWeekdays().size());
    }

    @Test
    @DisplayName("Добавление с ошибочным периодом дат")
    void addTripPurposeAndDateErrorPeriod() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        TripPurposeDateDTO tripPurposeDateDTO = new TripPurposeDateDTO();
        tripPurposeDateDTO.setStartDate(LocalDateTime.now().plusDays(2));
        tripPurposeDateDTO.setEndDate(LocalDateTime.now());

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder().label(label)
                .tripPurposeDate(tripPurposeDateDTO)
                .build();


        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        mockMvc.perform(post(Strings.concat("/", organization.getId(), "/purposes"))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().is(409));
    }

    @Test
    @DisplayName("Добавление с одинаковым периодом дат")
    void addTripPurposeAndDateEqualPeriod() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        TripPurposeDateDTO tripPurposeDateDTO = new TripPurposeDateDTO();
        tripPurposeDateDTO.setStartDate(LocalDateTime.now());
        tripPurposeDateDTO.setEndDate(LocalDateTime.now());

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder().label(label)
                .tripPurposeDate(tripPurposeDateDTO)
                .build();


        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        mockMvc.perform(
                        post(Strings.concat("/", organization.getId(), "/purposes")).contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Добавление с ошибочным периодом времени")
    void addTripPurposeAndTimeErrorPeriod() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        TripPurposeTimeRangeDTO tripPurposeTimeRangeDTO = new TripPurposeTimeRangeDTO();
        tripPurposeTimeRangeDTO.setStartTime(LocalDateTime.now().plusHours(4));
        tripPurposeTimeRangeDTO.setEndTime(LocalDateTime.now().plusHours(1));

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder().label(label)
                .tripPurposeTime(tripPurposeTimeRangeDTO)
                .build();

        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        mockMvc.perform(
                        post(Strings.concat("/", organization.getId(), "/purposes")).contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().is(200));
    }

    @Test
    @DisplayName("Добавление с одинаковым периодом времени")
    void addTripPurposeAndTimeEqualPeriod() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        TripPurposeTimeRangeDTO tripPurposeTimeRangeDTO = new TripPurposeTimeRangeDTO();
        tripPurposeTimeRangeDTO.setStartTime(LocalDateTime.now());
        tripPurposeTimeRangeDTO.setEndTime(LocalDateTime.now().plusMinutes(10));

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder().label(label)
                .tripPurposeTime(tripPurposeTimeRangeDTO)
                .build();


        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        mockMvc.perform(
                        post(Strings.concat("/", organization.getId(), "/purposes")).contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Добавление с всеми фильтрами")
    void addTripPurposeAllFilter() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        Attribute attribute = new Attribute();
        attribute.setName("attribute one");
        attributeRepository.save(attribute);

        TripPurposeDateDTO tripPurposeDateDTO = new TripPurposeDateDTO();
        tripPurposeDateDTO.setStartDate(LocalDateTime.now());
        tripPurposeDateDTO.setEndDate(LocalDateTime.now().plusDays(2));

        TripPurposeWeekdayDTO tripPurposeWeekdayDTO = new TripPurposeWeekdayDTO();
        tripPurposeWeekdayDTO.setWeekday(DayOfWeek.MONDAY);

        TripPurposeTimeRangeDTO tripPurposeTimeRangeDTO = new TripPurposeTimeRangeDTO();
        tripPurposeTimeRangeDTO.setStartTime(LocalDateTime.now().plusHours(1));
        tripPurposeTimeRangeDTO.setEndTime(LocalDateTime.now().plusHours(4));

        var idContainerDTO = IdContainerDTO.builder().id(attribute.getId()).build();

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder()
                .label(label)
                .tripPurposeDate(tripPurposeDateDTO)
                .tripPurposeWeekday(tripPurposeWeekdayDTO)
                .tripPurposeTime(tripPurposeTimeRangeDTO)
                .tripPurposeAttribute(idContainerDTO).build();

        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        var response = mockMvc.perform(
                        post(Strings.concat("/", organization.getId(), "/purposes")).contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<LinkedHashMap<String, Object>>() {
                });

        assertNotNull(actual.get("tripPurposeDates"));
        assertNotNull(actual.get("tripPurposeWeekdays"));
        assertNotNull(actual.get("tripPurposeAttributes"));
        assertNotNull(actual.get("tripPurposeWeekdays"));
        assertNotNull(actual.get("tripPurposeTimes"));
    }

    @Test
    @DisplayName("Получение по id")
    void getTripPurposeById() throws Exception {
        TripPurpose tripPurpose = new TripPurpose();
        tripPurpose.setLabel("trip purpose one");
        tripPurpose.setOrganization(testOrganization1);
        tripPurposeRepository.save(tripPurpose);

        var result = mockMvc.perform(
                        get(Strings.concat("/", testOrganization1.getId(), "/purposes/", tripPurpose.getId()))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> objectMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(objectMap.get("id"), tripPurpose.getId().toString());
    }

    @Test
    @DisplayName("Получение по вхождению name")
    void getTripPurposeLikeName() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        Organization organizationTwo = new Organization();
        organizationTwo.setAddress("address two");
        organizationTwo.setOfficialName("official name two");
        organizationTwo.setMsrn("msrn");
        organizationTwo.setTid("tid");
        organizationTwo.setDigitId(2L);
        organizationRepository.save(organizationTwo);

        TripPurpose tripPurpose = new TripPurpose();
        tripPurpose.setLabel("trip purpose one");
        tripPurpose.setOrganization(organization);
        tripPurposeRepository.save(tripPurpose);

        TripPurpose tripTwoPurpose = new TripPurpose();
        tripTwoPurpose.setLabel("trip purpose two");
        tripTwoPurpose.setOrganization(organization);
        tripPurposeRepository.save(tripTwoPurpose);

        TripPurpose tripThreePurpose = new TripPurpose();
        tripThreePurpose.setLabel("trip purpose two");
        tripThreePurpose.setOrganization(organizationTwo);
        tripPurposeRepository.save(tripThreePurpose);

        var result = mockMvc.perform(
                        get(Strings.concat("/", organization.getId(), "/purposes/search?value=two"))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();

        List<Map<String, Object>> objectMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(1, objectMap.size());
        assertEquals(objectMap.getFirst().get("id"), tripTwoPurpose.getId().toString());
    }

    @Test
    @DisplayName("Изменение")
    void editTripPurpose() throws Exception {
        Organization organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setDigitId(1L);
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        TripPurpose tripPurpose = new TripPurpose();
        String newTripPurpose = "New trip purpose";
        tripPurpose.setLabel(newTripPurpose);
        tripPurpose.setOrganization(organization);
        tripPurposeRepository.save(tripPurpose);

        TripPurposeDTO updatedDTO = new TripPurposeDTO();
        updatedDTO.setId(tripPurpose.getId());
        String editTripPurpose = "Edit trip purpose";
        updatedDTO.setLabel(editTripPurpose);
        updatedDTO.setPurposeType(TripPurposeType.PERSONAL);
        updatedDTO.setTripPurposeAttributes(Collections.emptyList());
        updatedDTO.setTripPurposeDepartments(Collections.emptyList());
        updatedDTO.setTripPurposeDates(Collections.emptyList());
        updatedDTO.setTripPurposeTimes(Collections.emptyList());
        updatedDTO.setTripPurposeWeekdays(Collections.emptyList());

        var request = objectMapper.writeValueAsString(updatedDTO);
        mockMvc.perform(
                        put(Strings.concat("/", organization.getId(), "/purposes/", tripPurpose.getId()))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON).content(request)).
                andExpect(status().isOk()).andReturn();

        TripPurpose tripPurposeEdit =
                tripPurposeRepository.findByLabelAndOrganization(editTripPurpose, organization.getId()).orElseThrow();
        assertNotEquals(updatedDTO.getId(), tripPurposeEdit.getId());
        assertEquals(updatedDTO.getLabel(), tripPurposeEdit.getLabel());
        assertEquals(updatedDTO.getPurposeType(), tripPurposeEdit.getPurposeType());

        var messages = getMessages(purposeOutput, 2, TripPurposeMessage.class);
        TripPurposeMessage deleteMessage;
        TripPurposeMessage addMessage;
        var firstMessage = messages.get(0);
        var secondMessage = messages.get(1);
        if (firstMessage.isDeleted()) {
            deleteMessage = firstMessage;
            addMessage = secondMessage;
        } else {
            deleteMessage = secondMessage;
            addMessage = firstMessage;
        }

        assertTrue(deleteMessage.isDeleted());
        assertEquals(deleteMessage.getId(), tripPurpose.getId());

        assertFalse(addMessage.isDeleted());
        assertEquals(addMessage.getId(), tripPurposeEdit.getId());
        assertEquals(addMessage.getLabel(), editTripPurpose);
        assertEquals(addMessage.getOrganization(), tripPurposeEdit.getOrganization().getId());
    }

    @Test
    @DisplayName("Получение всех целей поездок органицации")
    void getAllTripPurpose() throws Exception {
        Organization organizationOne = new Organization();
        organizationOne.setAddress("address one");
        organizationOne.setOfficialName("official name one");
        organizationOne.setMsrn("msrn");
        organizationOne.setTid("tid");
        organizationOne.setDigitId(1L);
        organizationOne = organizationRepository.save(organizationOne);
        testDepartment1.setOrganization(organizationOne);

        testPosition1.setOrganization(organizationOne);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organizationOne);
        employeeRepository.save(testEmployee1);

        Organization organizationTwo = new Organization();
        organizationTwo.setAddress("address two");
        organizationTwo.setOfficialName("official name two");
        organizationTwo.setMsrn("msrn");
        organizationTwo.setTid("tid");
        organizationTwo.setDigitId(2L);
        organizationRepository.save(organizationTwo);

        TripPurpose tripPurposeOne = new TripPurpose();
        tripPurposeOne.setLabel("trip purpose one");
        tripPurposeOne.setOrganization(organizationOne);
        tripPurposeRepository.save(tripPurposeOne);

        TripPurpose tripPurposeTwo = new TripPurpose();
        tripPurposeTwo.setLabel("trip purpose two");
        tripPurposeTwo.setOrganization(organizationOne);
        tripPurposeRepository.save(tripPurposeTwo);

        TripPurpose tripPurposeThree = new TripPurpose();
        tripPurposeThree.setLabel("trip purpose three");
        tripPurposeThree.setOrganization(organizationTwo);
        tripPurposeRepository.save(tripPurposeThree);

        TripPurpose tripPurposeFour = new TripPurpose();
        tripPurposeFour.setLabel("trip purpose four");
        tripPurposeFour.setOrganization(organizationOne);
        tripPurposeFour.setActive(false);
        tripPurposeRepository.save(tripPurposeFour);

        var result = mockMvc.perform(get(Strings.concat("/", organizationOne.getId(), "/purposes"))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();

        List<Map<String, Object>> objectMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        for (Map<String, Object> map : objectMap) {
            UUID id = UUID.fromString(String.valueOf(map.get("id")));
            TripPurpose expected = tripPurposeRepository.findById(id).orElseThrow();
            assertThat(map).containsEntry("label", expected.getLabel());
        }
        mockMvc.perform(get(Strings.concat("/", organizationOne.getId(), "/purposes/all"))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();
    }

    @Test
    @DisplayName("Удаление")
    void deleteTripPurpose() throws Exception {
        var organization = new Organization();
        organization.setAddress("address");
        organization.setOfficialName("official name");
        organization.setDigitId(1L);
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization = organizationRepository.save(organization);
        testDepartment1.setOrganization(organization);

        testPosition1.setOrganization(organization);
        testPosition1 = positionRepository.save(testPosition1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(organization);
        employeeRepository.save(testEmployee1);

        var tripPurpose = new TripPurpose();
        String newTripPurpose = "New trip purpose";
        tripPurpose.setLabel(newTripPurpose);
        tripPurpose.setOrganization(organization);
        tripPurposeRepository.save(tripPurpose);

        mockMvc.perform(delete(Strings.concat("/", organization.getId(), "/purposes/", tripPurpose.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        var optionalTripPurpose =
                tripPurposeRepository.findByIdAndOrganization(tripPurpose.getId(), organization.getId());
        assertFalse(optionalTripPurpose.isPresent());

        var deleteTripPurpose = tripPurposeRepository.findById(tripPurpose.getId());
        assertTrue(deleteTripPurpose.isEmpty());
    }

    @Test
    @DisplayName("Поиск целей у сотрудника")
    void getTripPurposeByUserAttribute() throws Exception {
        Attribute attribute = new Attribute();
        attribute.setName("attribute one");
        attribute.getEmployees().add(testEmployee1);
        attributeRepository.save(attribute);

        testEmployee1.getAttributes().add(attribute);
        employeeRepository.save(testEmployee1);

        var idContainerDTO = IdContainerDTO.builder().id(attribute.getId()).build();

        String label = "New trip purpose";
        NewTripPurposeDTO newTripPurposeDTO = NewTripPurposeDTO.builder().label(label)
                .tripPurposeAttribute(idContainerDTO).build();

        var request = objectMapper.writeValueAsString(newTripPurposeDTO);
        mockMvc.perform(post(Strings.concat("/", testOrganization1.getId(), "/purposes"))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk()).andReturn();

        var tripPurposeDateDTO = TripPurposeDateDTO.builder()
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        NewTripPurposeDTO tripPurposeDTODates = NewTripPurposeDTO.builder().label("Trip purpose dates")
                .tripPurposeDate(tripPurposeDateDTO).build();

        var requestTwo = objectMapper.writeValueAsString(tripPurposeDTODates);
        mockMvc.perform(post(Strings.concat("/", testOrganization1.getId(), "/purposes"))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON).content(requestTwo))
                .andExpect(status().isOk()).andReturn();

        var result =
                mockMvc.perform(get(Strings.concat("/", testOrganization1.getId(), "/purposes/search_by_employee"))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andReturn();

        List<Map<String, Object>> objectMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(2, objectMap.size());

        Map<String, Object> attributeMap = objectMap.getFirst();
        Object tripPurposeAttributes = attributeMap.get("tripPurposeAttributes");
        assertFalse(((List<?>) tripPurposeAttributes).isEmpty());

        Object tripPurposeAttributesDepartments = attributeMap.get("tripPurposeDepartments");
        Object tripPurposeAttributesWeekdays = attributeMap.get("tripPurposeWeekdays");
        Object tripPurposeAttributesTimes = attributeMap.get("tripPurposeTimes");
        Object tripPurposeAttributesDates = attributeMap.get("tripPurposeDates");
        assertTrue(((List<?>) tripPurposeAttributesDepartments).isEmpty());
        assertTrue(((List<?>) tripPurposeAttributesWeekdays).isEmpty());
        assertTrue(((List<?>) tripPurposeAttributesTimes).isEmpty());
        assertTrue(((List<?>) tripPurposeAttributesDates).isEmpty());

        Map<String, Object> dateMap = objectMap.get(1);
        Object tripPurposeDates = dateMap.get("tripPurposeDates");
        assertFalse(((List<?>) tripPurposeDates).isEmpty());

        Object tripPurposeDatesAttributes = dateMap.get("tripPurposeAttributes");
        Object tripPurposeDepartments = dateMap.get("tripPurposeDepartments");
        Object tripPurposeWeekdays = dateMap.get("tripPurposeWeekdays");
        Object tripPurposeTimes = dateMap.get("tripPurposeTimes");
        assertTrue(((List<?>) tripPurposeDatesAttributes).isEmpty());
        assertTrue(((List<?>) tripPurposeDepartments).isEmpty());
        assertTrue(((List<?>) tripPurposeWeekdays).isEmpty());
        assertTrue(((List<?>) tripPurposeTimes).isEmpty());
    }

    @Test
    @DisplayName("Поиск целей у сотрудника с пустыми атрибутами")
    void getTripPurposeByUserEmptyAttribute() throws Exception {
        var tripPurposeDateDTO = TripPurposeDateDTO.builder()
                .startDate(LocalDateTime.now()).build();
        tripPurposeDateDTO.setEndDate(LocalDateTime.now().plusDays(2));

        NewTripPurposeDTO tripPurposeDTODates = NewTripPurposeDTO.builder()
                .label("Trip purpose dates")
                .tripPurposeDate(tripPurposeDateDTO)
                .build();

        var requestTwo = objectMapper.writeValueAsString(tripPurposeDTODates);
        mockMvc.perform(post(Strings.concat("/", testOrganization1.getId(), "/purposes"))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON).content(requestTwo))
                .andExpect(status().isOk()).andReturn();

        var result =
                mockMvc.perform(get(Strings.concat("/", testOrganization1.getId(), "/purposes/search_by_employee"))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andReturn();

        List<Map<String, Object>> objectMap = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        Map<String, Object> dateMap = objectMap.getFirst();
        Object tripPurposeDates = dateMap.get("tripPurposeDates");
        assertFalse(((List<?>) tripPurposeDates).isEmpty());

        Object tripPurposeDatesAttributes = dateMap.get("tripPurposeAttributes");
        Object tripPurposeDepartments = dateMap.get("tripPurposeDepartments");
        Object tripPurposeWeekdays = dateMap.get("tripPurposeWeekdays");
        Object tripPurposeTimes = dateMap.get("tripPurposeTimes");
        assertTrue(((List<?>) tripPurposeDatesAttributes).isEmpty());
        assertTrue(((List<?>) tripPurposeDepartments).isEmpty());
        assertTrue(((List<?>) tripPurposeWeekdays).isEmpty());
        assertTrue(((List<?>) tripPurposeTimes).isEmpty());
    }

}
