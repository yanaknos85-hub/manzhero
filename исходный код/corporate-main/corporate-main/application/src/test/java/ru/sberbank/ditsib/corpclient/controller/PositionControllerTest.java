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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.authorization.service.CheckUserAccessService;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionSearchDTO;
import ru.sberbank.ditsib.corpclient.mapper.PositionMapper;
import ru.sberbank.ditsib.corpclient.service.import_easup.AutoApproveService;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера должностей")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
class PositionControllerTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private AutoApproveService autoApproveService;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PositionRepository positionRepository;

    @MockitoBean(name = "positionOutput")
    private OutputBridge positionOutput;

    @MockitoSpyBean
    private CheckUserAccessService userAccessService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @BeforeEach
    public void persistNeeded() {
        testOrganization1.setDigitId(1L);
        testOrganization2.setDigitId(2L);
        organizationRepository.save(testOrganization1);
        organizationRepository.save(testOrganization2);
    }
    
    @Test
    @DisplayName("Добавление должности")
    void test_addPosition() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setUserId(testEmployee1.getId());
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);

        testPosition2.setOrganization(testOrganization1);
        var newPosition = testPosition2;
        var request = objectMapper.writeValueAsString(positionMapper.positionToNewDTO(newPosition));
        
        var response = mockMvc.perform(
                                      post(PositionController.getApiMappingByOrgId(testOrganization1.getId()))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                              .content(request))
                              .andExpect(status().isOk()).andReturn();
        
        PositionDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                    PositionDTO.class);
        
        var expected = positionRepository.findAll().get(1);
        
        assertNotNull(expected.getHumanReadableId());
        assertEquals(expected.getHumanReadableId(),actual.getHumanReadableId());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getOrganization().getId(), actual.getOrganizationId());
        
        var actualMessage = getMessages(positionOutput, PositionMessage.class);
        
        assertEquals(expected.getId(), actualMessage.getId());
        assertEquals(expected.getName(), actualMessage.getPositionName());
        assertEquals(expected.getOrganization().getId(), actualMessage.getOrganizationId());
        
        
        var response2 = mockMvc.perform(post(PositionController.getApiMappingByOrgId(testOrganization1.getId())
                                             + "searchPositions")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                                .content(objectMapper.writeValueAsString(new PositionSearchDTO()))
                                                .contentType(MediaType.APPLICATION_JSON))
                               .andExpect(status().isOk()).andReturn();
        
        List<PositionDTO> found2 =
                objectMapper.readValue(response2.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        
        assertThat(found2).hasSize((int) positionRepository.count());
        
        var searchDto = PositionSearchDTO.builder()
                                         .positionName(POSITION1_NAME)
                                         .availableClasses(Collections.singleton(TaxiClass.ECONOMY))
                                         .build();
        var response3 = mockMvc.perform(post(PositionController.getApiMappingByOrgId(testOrganization1.getId())
                                             + "searchPositions")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                                .content(objectMapper.writeValueAsString(searchDto))
                                                .contentType(MediaType.APPLICATION_JSON))
                               .andExpect(status().isOk()).andReturn();
        List<PositionDTO> found3 =
                objectMapper.readValue(response3.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertThat(found3).hasSize(1);
    }
    
    @Test
    @DisplayName("Добавление должности - длинное наименование")
    void test_addPositionLongName() throws Exception {
        var resolvedException = mockMvc.perform(
                                               post(PositionController.getApiMappingByOrgId(testOrganization1.getId()))
                                                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                                       .andExpect(status().isBadRequest()).andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(HttpMessageNotReadableException.class, resolvedException.getClass());
    }
    
    @Test
    @DisplayName("Обновление должности")
    void test_updatePosition() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);
        
        testPosition1.setHumanReadableId("PS-002-1");
        testPosition1.setOrganization(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        positionRepository.save(testPosition1);
        var old = positionRepository.findAll().getFirst();
        
        PositionDTO updated = positionMapper.positionToDTO(old);
        
        updated.setName(SharedData.POSITION2_NAME);
        updated.setSelfApproved(true);
        mockMvc.perform(put(PositionController.getApiMappingByOrgId(testOrganization1.getId())
                            + testPosition1.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(objectMapper.writeValueAsString(updated)))
               .andExpect(status().isOk()).andReturn();

        var current = getMessages(positionOutput, PositionMessage.class);
        
        assertEquals(testPosition1.getId(), current.getId());
        assertEquals(SharedData.POSITION2_NAME, current.getPositionName());
    }
    
    @Test
    @DisplayName("Удаление должности")
    void test_deletePosition() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);

        testPosition1.setHumanReadableId("PS-001-1");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition2.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);
        
        var count = positionRepository.findAll().size();
        assertTrue(count > 0);
        mockMvc.perform(delete(PositionController.getApiMappingByOrgId(testOrganization1.getId())
                               + testPosition2.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        
        assertEquals(count - 1, (int) positionRepository.findAll().stream().map(Position::getActiveStatus).filter(ActiveStatus.ACTIVE::equals).count());
        
        var actualMessage = getMessages(positionOutput, PositionMessage.class);
        
        assertEquals(testPosition2.getId(), actualMessage.getId());
        assertTrue(actualMessage.isDeleted());
        
    }
    
    @Test
    @DisplayName("Удаление  несуществующей должности")
    void test_deleteNotExistingPosition() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);
        
        UUID rndUUID = UUID.randomUUID();
        Exception resolvedException =
                mockMvc.perform(delete(PositionController.getApiMappingByOrgId(testOrganization1.getId())
                                       + rndUUID)
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                       .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(EntityNotFoundException.class, resolvedException.getClass());
        
    }
    
    
    @Test
    @DisplayName("Получение  существующей должности")
    void test_getPosition() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testEmployee1.getDepartment().getOrganization());
        employeeRepository.save(testEmployee1);
        
        testPosition2.setHumanReadableId("PS-002-1");
        testPosition2.setOrganization(testOrganization1);
        positionRepository.save(testPosition2);
        assertTrue(positionRepository.findById(testPosition2.getId()).isPresent());
        
        var saved =
                mockMvc.perform(
                               get(PositionController.getApiMappingByOrgId(testOrganization1.getId())
                                   + testPosition2.getId().toString())
                                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                       .andExpect(status().isOk()).andReturn();
        assertNotNull(saved);
        PositionDTO actual = objectMapper.readValue(saved.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                    PositionDTO.class);
        
        
        assertEquals(testPosition2.getId(), actual.getId());
        assertEquals(testPosition2.getName(), actual.getName());
        assertEquals(testPosition2.getOrganization().getId(), actual.getOrganizationId());
    }
    
    @Test
    @DisplayName("Получение  несуществующей должности")
    void test_getAbsentPosition() throws Exception {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee1);
        
        String rndUUID = UUID.randomUUID().toString();
        Exception resolvedException = mockMvc.perform(
                                                     get(PositionController.getApiMappingByOrgId(testOrganization1.getId())
                                                         + rndUUID)
                                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                             .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                                             .andExpect(status().isNotFound())
                                             .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Получение списка должностей")
    void test_getPositions() throws Exception {
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-002-1");
        // должность орг-ции 2
        testPosition3.setHumanReadableId("PS-003-1");
        
        testOrganization2 = organizationRepository.save(testOrganization2);
        testOrganization1 = organizationRepository.save(testOrganization1);

        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization1);
        testPosition3.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        positionRepository.save(testPosition3);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testDepartment2.setOrganization(testOrganization2);
        testDepartment2 = departmentRepository.save(testDepartment2);

        testEmployee1.setPosition(testPosition2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.saveAndFlush(testEmployee1);

        testEmployee2.setDepartment(testDepartment2);
        testEmployee2.setUserId(UUID.fromString(USER2_ID));
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testEmployee2.setPosition(testPosition3);
        employeeRepository.saveAndFlush(testEmployee2);

        testEmployee2.getSupervisor().setOrganization(testEmployee2.getSupervisor().getDepartment().getOrganization());
        employeeRepository.saveAndFlush(testEmployee2.getSupervisor());
        
        doNothing().when(userAccessService).check(testOrganization1.getId());
        
        var saved_1 = mockMvc.perform(
                                     get(PositionController.getApiMappingByOrgId(testOrganization1.getId()))
                                             .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                             .andExpect(status().isOk()).andReturn();
        assertNotNull(saved_1);
        List<PositionDTO> savedList_1 = objectMapper.readValue(saved_1.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                               new TypeReference<>() {
                                                               });
        assertEquals(2, savedList_1.size());
        
        doNothing().when(userAccessService).check(testOrganization2.getId());
        
        var saved_2 = mockMvc.perform(
                                     get(PositionController.getApiMappingByOrgId(testOrganization2.getId()))
                                             .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                             .andExpect(status().isOk()).andReturn();
        assertNotNull(saved_2);
        List<PositionDTO> savedList_2 = objectMapper.readValue(saved_2.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                               new TypeReference<>() {
                                                               });
        assertEquals(1, savedList_2.size());
    }

    @Test
    @DisplayName("Обновление должности по части автосогласования")
    void test_updatePositionAutoApprove() {
        testOrganization1 = organizationRepository.save(testOrganization1);
        testPosition1.setOrganization(testOrganization1);
        testPosition1.setSelfApproved(false);
        testPosition1.setHumanReadableId("PS-002-1");
        testPosition1 = positionRepository.save(testPosition1);

        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        testEmployee1.setPosition(testPosition1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        employeeRepository.saveAndFlush(testEmployee1);

        testDepartment1.setHead(testEmployee1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        var testPositionBefore = positionRepository.findById(testPosition1.getId());
        assertThat(testPositionBefore.orElseThrow().isSelfApproved()).isFalse();
        autoApproveService.run();
        var testPositionAfter = positionRepository.findById(testPosition1.getId());
        assertThat(testPositionAfter.orElseThrow().isSelfApproved()).isTrue();
    }
}
