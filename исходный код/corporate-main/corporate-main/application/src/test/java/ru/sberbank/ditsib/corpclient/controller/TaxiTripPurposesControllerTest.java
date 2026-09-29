package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера целей поездки")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@Transactional
@ActiveProfiles("test")
class TaxiTripPurposesControllerTest {
    private static final String USER1_ID = "00000000-0000-0000-0000-000000000000";
    @Autowired
    private TripPurposeRepository repository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    private Organization organization;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    void setup() {
        organization = new Organization();
        organization.setAddress("Address");
        organization.setOfficialName("Official name");
        organization.setMsrn("msrn");
        organization.setTid("tid");

        organization = organizationRepository.save(organization);

        var position = new Position();
        position.setName("Name");
        position.setOrganization(organization);
        position.setHumanReadableId("HRIP1");
        position = positionRepository.save(position);

        var department = Department.builder()
                .code("Code")
                .name("Name")
                .humanReadableId("HRI1")
                .organization(organization)
                .updateTime(OffsetDateTime.now())
                .build();

        department = departmentRepository.save(department);

        var employee = Employee.builder()
                .id(UUID.randomUUID())
                .firstName("First name")
                .lastName("Last name")
                .humanReadableId("HRIE1")
                .personnelNumber(Instancio.create(String.class))
                .position(position)
                .userId(UUID.fromString(USER1_ID))
                .department(department)
                .position(position)
                .organization(department.getOrganization())
                .updateTime(OffsetDateTime.now())
                .build();

        employeeRepository.save(employee);
    }

    @Test
    @DisplayName("Получение всех целей поездки")
    void test_getAll() throws Exception {
        var initialCount = repository.findAll().size();

        TripPurpose tripPurposeOne = TripPurpose.builder().organization(organization).label("один корона два").build();
        repository.save(tripPurposeOne);

        TripPurpose tripPurposeTwo = TripPurpose.builder().organization(organization).label("один два три").build();
        repository.save(tripPurposeTwo);

        assertEquals(2, repository.findAll().size() - initialCount);

        var response = mockMvc.perform(
                        get(String.format("/%s/purposes", organization.getId()))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        List<TripPurpose> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        assertEquals(repository.findAll().size() - initialCount, actual.size());
    }


    @Test
    @DisplayName("Поиск целей поездки по подстроке")
    void test_getTripPurposeBySearchString() throws Exception {
        TripPurpose tripPurposeOne = TripPurpose.builder().label("один корона два").organization(organization).build();
        repository.save(tripPurposeOne);

        TripPurpose tripPurposeTwo = TripPurpose.builder().label("один два три").organization(organization).build();
        repository.save(tripPurposeTwo);

        var response = mockMvc.perform(
                        get(String.format("/%s/purposes/search?value=корона", organization.getId()))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        List<TripPurpose> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        assertEquals(1, actual.size());
    }

    @Test
    @DisplayName("Получение по id")
    void test_getRequestById() throws Exception {
        TripPurpose tripPurpose = TripPurpose.builder().organization(organization).label("New purpose").build();
        tripPurpose = repository.save(tripPurpose);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(String.format("/%s/purposes/%s", organization.getId(), tripPurpose.getId()))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value(tripPurpose.getLabel()));
    }

    @Test
    @DisplayName("Получение по несуществующему id")
    void test_getRequestByRandomId() throws Exception {
        mockMvc.perform(
                        get("/purposes/" + UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound()).andReturn();

    }
}
