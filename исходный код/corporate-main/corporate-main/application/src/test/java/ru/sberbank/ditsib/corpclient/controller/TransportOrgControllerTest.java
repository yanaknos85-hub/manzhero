package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
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
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.dto.TransportOrgDto;
import ru.sberbank.ditsib.corpclient.dto.constant.TransportTypeEnumDTO;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка типов транспорта")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
class TransportOrgControllerTest extends SharedData {
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private PositionRepository positionRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @Test
    @DisplayName("Получение всех типов транспорта")
    void test_getAll() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);
        
        UUID orgId = testOrganization1.getId();
        // get initial transport types
        var response1 = mockMvc.perform(get("/transportorg/org/" + orgId)
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                               .andExpect(status().isOk()).andReturn();
        List<Map<String, String>> actual1 =
                objectMapper.readValue(response1.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(0, actual1.size());
        
        var response2 = mockMvc.perform(post("/transportorg/org/" + orgId + "/tt/" + TransportTypeEnum.TAXI.name())
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                               .andExpect(status().isOk()).andReturn();
        List<String> actual2 =
                objectMapper.readValue(response2.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(1, actual2.size());
        
        var response3 = mockMvc.perform(delete("/transportorg/org/" + orgId + "/tt/" + TransportTypeEnum.TAXI.name())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                               .andExpect(status().isOk()).andReturn();
        List<String> actual3 =
                objectMapper.readValue(response3.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(0, actual3.size());
    }

    @Test
    @DisplayName("Сохранение нескольких типов транспорта")
    void test_AddBatch() throws Exception {

        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        UUID orgId = testOrganization1.getId();

        var testTransportOrgDto1 = TransportOrgDto.builder()
                .transportType("DOMESTIC_COURIER")
                .active(false)
                .build();
        var testTransportOrgDto2 = TransportOrgDto.builder()
                .transportType("DEDICATED")
                .active(true)
                .build();

        // get initial transport types
        var response1 = mockMvc.perform(get("/transportorg/org/" + orgId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        List<TransportTypeEnumDTO> actual1 =
                objectMapper.readValue(response1.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(0, actual1.size());

        // add transport type DOMESTIC_COURIER
        var response2 = mockMvc.perform(post("/transportorg/org/" + orgId + "/tt/" + TransportTypeEnum.DOMESTIC_COURIER)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        List<TransportTypeEnum> actual2 =
                objectMapper.readValue(response2.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(1, actual2.size());

        // get updated transport types
        var response3 = mockMvc.perform(get("/transportorg/org/" + orgId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        List<TransportTypeEnumDTO> actual3 =
                objectMapper.readValue(response3.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(1, actual3.size());
        assertEquals(testTransportOrgDto1.getTransportType(), actual3.getFirst().getName());

        // add batch (1 add, 1 delete)
        var dtos = objectMapper.writeValueAsString(List.of(testTransportOrgDto1, testTransportOrgDto2));
        mockMvc.perform(post("/transportorg/batch/org/" + orgId)
                        .with(jwt().jwt(builder -> builder.claim("data_master", true)
                                        .jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(dtos)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                        )
                .andExpect(status().isOk()).andReturn();

        // get updated transport types
        var response4 = mockMvc.perform(get("/transportorg/org/" + orgId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        List<TransportTypeEnumDTO> actual4 =
                objectMapper.readValue(response4.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(1, actual4.size());
        assertEquals(testTransportOrgDto2.getTransportType(), actual4.getFirst().getName());
    }
}
