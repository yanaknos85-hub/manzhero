package ru.sberbank.ditsib.transport.approvals.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import ru.sber.transport.approvals.messaging.ApprovalsSettingsMessage;
import ru.sber.transport.approvals.messaging.OtherTrTypesApprovalsSettingsMessage;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;
import ru.sberbank.ditsib.transport.approvals.database.model.OtherTrTypesApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.database.model.Position;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewOtherTrTypesApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewPurposeAndRegionApprovalSettingsItemDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.OtherTrTypesApprovalsSettingsDTO;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DisplayName("Проверка контроллера настроек согласования транспорта, кроме общественного и такси")
@EmbeddedPostgres
@MockitoBean(types = {JwtDecoder.class})
class OtherTrTypesApprovalsSettingsControllerTest extends ApprovalsSettingsSharedTestData {
    
    private static final String OTHER_TR_APPROVAL_SETTINGS_BASE_URL = "/{organizationId}/settings/request/other/";
    private final TestSharedData sharedData = new TestSharedData();
    private OtherTrTypesApprovalsSettings otherTrTypesApprovalsSettings;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OtherTrTypesApprovalsSettingsRepository settingsRepository;

    @MockitoBean("approvalsSettingsOutput")
    private OutputBridge approvalsSettingsOutput;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TripPurposeRepository purposeRepository;
    
    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @BeforeEach
    void createAndPersistEntities() {
        geoZoneRepository.saveAll(List.of(geoZone1, geoZone2));
        organizationRepository.saveAll(Arrays.asList(organization1, organization2));
        var position1 = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organization1.getId())
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        var position2 = positionRepository.save(Instancio.of(Position.class)
                .set(field(Position::getOrganizationId), organization2.getId())
                .set(field(Position::isActive), true)
                .set(field(Position::isSelfApproved), false)
                .create()
        );
        purposeRepository.saveAll(Arrays.asList(purpose1, purpose2));
    
        otherTrTypesApprovalsSettings = OtherTrTypesApprovalsSettings.builder()
                                                                     .approvalActive(false)
                                                                     .minCostToBeApproved(500)
                                                                     .organization(organization1)
                                                                     .transportType("BICYCLE")
                                                                     .purposeAndRegionItems(Arrays.asList(item1, item2))
                                                                     .tripApprovalActive(true)
                                                                     .build();
        settingsRepository.save(otherTrTypesApprovalsSettings);

        var department = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organization1.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department.getId())
                .set(field(Employee::getPositionId), position1.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .create());

        var department2 = departmentRepository.save(Instancio.of(Department.class)
                .set(field(Department::getOrganizationId), organization2.getId())
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create());
        employeeRepository.save(Instancio.of(Employee.class)
                .set(field(Employee::getDepartmentId), department2.getId())
                .set(field(Employee::getPositionId), position2.getId())
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .set(field(Employee::getUserId), UUID.fromString(USER2_ID))
                .create());
    }
    
    @AfterEach()
    void afterEach() {
        settingsRepository.deleteAll();
        purposeRepository.deleteAll();
        geoZoneRepository.deleteAll();
        departmentRepository.findAll().forEach(department -> departmentRepository.save(department
                .setDepartmentHeadId(null)
                .setParentId(null)));
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        organizationRepository.deleteAll();
    }
    
    @DisplayName("Создание новой настройки согласования поездок транспорте, кроме общественного и такси с " +
                 "некорректными данными")
    @Test
    void testOtherTrTypesApprovalsSettingsCreateWithPurposeId() throws Exception {
        var newPurposeAndRegionDTO = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                          //null - incorrect value
                                                          .purposeId(null)
                                                          .regionId(UUID.randomUUID())
                                                          .minCostToBeApproved(250)
                                                          .build();
        var newPurposeAndRegionDTO1 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                          .purposeId(purpose2.getId())
                                                          //null - incorrect value
                                                          .regionId(null)
                                                          .minCostToBeApproved(250)
                                                          .build();
        var modelWithIncorrectPurpose = createNewDTO(Collections.singletonList(newPurposeAndRegionDTO), "BICYCLE", 500, true, true);
        var requestWithIncorrectPurpose = objectMapper.writeValueAsString(modelWithIncorrectPurpose);
        var modelWithIncorrectRegion = createNewDTO(Collections.singletonList(newPurposeAndRegionDTO1), "BICYCLE", 500, true, true);
        objectMapper.writeValueAsString(modelWithIncorrectRegion);
        var modelWithIncorrectItems = createNewDTO(null, "BICYCLE", 500, true, true);
        objectMapper.writeValueAsString(modelWithIncorrectItems);
        createNewDTO(Collections.singletonList(newPurposeAndRegionDTO), "TAXI", 500, true, true);
        objectMapper.writeValueAsString(modelWithIncorrectPurpose);
        assertThat(settingsRepository.count()).isEqualTo(1);
        sendCreateRequest(requestWithIncorrectPurpose, organization1.getId(), USER1_ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("purposeAndRegionItems[0].purposeId"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"));
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
    
    @DisplayName("Создание новой настройки согласования поездок транспорте, кроме общественного и такси. Нет настроек")
    @Test
    void testOtherTrTypesApprovalsSettingsCreateWithPurposeId_noSettings() throws Exception {
        NewPurposeAndRegionApprovalSettingsItemDTO newPurposeAndRegionDTO =
                NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                          //null - incorrect value
                                                          .purposeId(null)
                                                          .regionId(UUID.randomUUID())
                                                          .minCostToBeApproved(250)
                                                          .build();
        
        NewPurposeAndRegionApprovalSettingsItemDTO newPurposeAndRegionDTO1 =
                NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                          .purposeId(purpose2.getId())
                                                          //null - incorrect value
                                                          .regionId(null)
                                                          .minCostToBeApproved(250)
                                                          .build();
        
        NewOtherTrTypesApprovalsSettingsDTO modelWithIncorrectPurpose = createNewDTO(Collections.singletonList(newPurposeAndRegionDTO), "BICYCLE", 500, true, true);

        objectMapper.writeValueAsString(modelWithIncorrectPurpose);

        NewOtherTrTypesApprovalsSettingsDTO modelWithIncorrectRegion = createNewDTO(Collections.singletonList(newPurposeAndRegionDTO1), "BICYCLE", 500, true, true);

        objectMapper.writeValueAsString(modelWithIncorrectRegion);

        NewOtherTrTypesApprovalsSettingsDTO modelWithIncorrectItems = createNewDTO(null, "BICYCLE", 500, true, true);

        String requestWithIncorrectItems = objectMapper.writeValueAsString(modelWithIncorrectItems);
        
        createNewDTO(Collections.singletonList(newPurposeAndRegionDTO), "TAXI", 500, true, true);
        String requestWithIncorrectTransportType = objectMapper.writeValueAsString(modelWithIncorrectPurpose);
    
        // доп. настроек по целям поездки может и не быть
        sendCreateRequest(requestWithIncorrectItems, organization2.getId(), USER2_ID)
                .andExpect(status().isOk());
        
        sendCreateRequest(requestWithIncorrectTransportType, UUID.randomUUID(), USER2_ID).andExpect(status().isBadRequest()).andReturn();
        assertThat(settingsRepository.count()).isEqualTo(2);
    }
    
    @DisplayName("Создание новой настройки согласования поездок транспорте, кроме общественного и такси для " +
                 "организации, у которой настройка уже есть")
    @Test
    void testAlreadyExistingOtherTrTypesApprovalsSettingsCreate() throws Exception {
        NewOtherTrTypesApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), "BICYCLE", 500, true, true);
        String request = objectMapper.writeValueAsString(value);
        
        assertThat(settingsRepository.count()).isEqualTo(1);
        
        mockMvc.perform(
                post(OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .content(request)
                        .contentType(
                                MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isConflict()).andReturn();
        
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
    
    @DisplayName("Создание новой настройки согласования поездок транспорте, кроме общественного и такси")
    @Test
    void testOtherTrTypesApprovalsSettingsCreate() throws Exception {
        NewOtherTrTypesApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2),
                "BICYCLE", 500, false, false);
        String request = objectMapper.writeValueAsString(value);
        mockMvc.perform(
                post(OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization2.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID).claim("roles", "ROLE_USER")))
                        .content(request)
                        .contentType(
                                MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        OtherTrTypesApprovalsSettings settings =
                settingsRepository.findAllByOrganizationId(organization2.getId())
                                  .stream()
                                  .filter(s -> s.getTransportType().equals("BICYCLE"))
                                  .toList().getFirst();
        compare(settings, value);

        final var messageCaptor = ArgumentCaptor.forClass(OtherTrTypesApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.otherTrCheck(message, settings);
    }
    
    
    @DisplayName("Чтение несуществующей настройки согласования поездок транспорте, кроме общественного и такси " +
                 "(создаётся новая дефолтная настройка)")
    @Test
    void testNonExistingOtherTrTypesApprovalsSettingsGet() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        mockMvc.perform(
                get(OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization2.getId().toString())
                    + otherTrTypesApprovalsSettings.getTransportType())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID).claim("roles", "ROLE_USER"))))
               .andExpect(status().isOk())
               .andReturn();
    }
    
    @DisplayName("Чтение настройки согласования поездок транспорте, кроме общественного и такси")
    @Test
    void testOtherTrTypesApprovalsSettingsGet() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        MvcResult result = mockMvc.perform(
                get(OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString()) +
                    otherTrTypesApprovalsSettings.getTransportType())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                                  .andExpect(status().isOk())
                                  .andReturn();
        OtherTrTypesApprovalsSettingsDTO actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                     new TypeReference<>() {});
        
        assertThat(actual.getTransportType()).isEqualTo(otherTrTypesApprovalsSettings.getTransportType());
        assertThat(actual.getOrganizationId()).isEqualTo(otherTrTypesApprovalsSettings.getOrganization().getId());
        assertThat(actual.getMinCostToBeApproved()).isEqualTo(otherTrTypesApprovalsSettings.getMinCostToBeApproved());
        for (int i = 0; i < actual.getPurposeAndRegionItems().size(); i++) {
            var actualItem = actual.getPurposeAndRegionItems().get(i);
            var expectedItem = otherTrTypesApprovalsSettings.getPurposeAndRegionItems().get(i);
            assertThat(expectedItem.getRegion().getId()).isEqualTo(actualItem.getRegion().getId());
            assertThat(expectedItem.getRegion().getName()).isEqualTo(actualItem.getRegion().getName());
            assertThat(expectedItem.getTripPurpose().getId()).isEqualTo(actualItem.getTripPurpose().getId());
            assertThat(expectedItem.getTripPurpose().getLabel()).isEqualTo(actualItem.getTripPurpose().getLabel());
            assertThat(expectedItem.getMinCostToBeApproved()).isEqualTo(actualItem.getMinCostToBeApproved());
        }
        assertThat(actual.isTripApprovalActive()).isEqualTo(otherTrTypesApprovalsSettings.isTripApprovalActive());
    }
    
    @DisplayName("Изменение несуществующей настройки согласования поездок транспорте, кроме общественного и такси " +
                 "(создаётся новая дефолтная настройка)")
    @Test
    void testNotExistingOtherTrTypesApprovalsSettingsUpdate() {
        createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2),
                "BICYCLE", 250, true, true);
        
        OtherTrTypesApprovalsSettings settings =
                settingsRepository.findByOrganizationIdAndTransportType(organization1.getId(),
                                                                        otherTrTypesApprovalsSettings.getTransportType())
                                  .orElseThrow();
        compare(settings, otherTrTypesApprovalsSettings);
    }
    
    @DisplayName("Изменение настройки согласования поездок транспорте, кроме общественного и такси")
    @Test
    void testOtherTrTypesApprovalsSettingsUpdate() throws Exception {
        NewOtherTrTypesApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2),
                "BICYCLE", 250, true, true);
        String request = objectMapper.writeValueAsString(value);
        mockMvc.perform(
                put(OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString())
                    + otherTrTypesApprovalsSettings.getTransportType())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
    
        OtherTrTypesApprovalsSettings settings =
                settingsRepository.findByOrganizationIdAndTransportType(organization1.getId(),
                                                                        value.getTransportType())
                                  .stream()
                                  .toList().getFirst();
        compare(settings, value);

        final var messageCaptor = ArgumentCaptor.forClass(OtherTrTypesApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.otherTrCheck(message, settings);
    }
    
    
    @DisplayName("Удаление настройки согласования поездок транспорте, кроме общественного и такси")
    @Test
    void testOtherTrTypesApprovalsSettingsDelete() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        OtherTrTypesApprovalsSettings settings = settingsRepository.findAll().getFirst();
        final UUID settingsId = settings.getId();
        final String trType = settings.getTransportType();
        
        mockMvc.perform(delete(OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace(
                "{organizationId}", organization1.getId().toString()) + otherTrTypesApprovalsSettings.getTransportType())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER"))))
               .andExpect(status().isOk())
               .andReturn();
        assertThat(settingsRepository.count()).isZero();

        final var messageCaptor = ArgumentCaptor.forClass(ApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        assertThat(message.getId()).isEqualTo(settingsId);
        assertThat(message.getTransportType()).isEqualTo(trType);
        assertThat(message.isDeleted()).isTrue();
    }
    
    @DisplayName("Сброс несуществующей настройки поездок транспорте, кроме общественного и такси (создаётся новая с " +
                 "дефолтными настройками)")
    @Test
    void testNonExistingOtherTrTypesApprovalsSettingsRestore() throws Exception {
        OtherTrTypesApprovalsSettings settings =
                settingsRepository.findByOrganizationIdAndTransportType(organization1.getId(),
                                                                        otherTrTypesApprovalsSettings.getTransportType())
                                  .orElseThrow();
        
        assertThat(settings.isApprovalActive()).isFalse();
        assertThat(settings.getMinCostToBeApproved()).isEqualTo(500);
        
        mockMvc.perform(post(
                OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization2.getId().toString()) +
                otherTrTypesApprovalsSettings.getTransportType() + "/restore").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID).claim("roles", "ROLE_USER"))))
               .andExpect(status().isOk())
               .andReturn();
    }
    
    @DisplayName("Сброс настройки согласования поездок транспорте, кроме общественного и такси")
    @Test
    void testOtherTrTypesApprovalsSettingsRestore() throws Exception {
        OtherTrTypesApprovalsSettings settings =
                settingsRepository.findByOrganizationIdAndTransportType(organization1.getId(), otherTrTypesApprovalsSettings.getTransportType())
                                  .stream()
                                  .toList().getFirst();
        
        compare(settings, otherTrTypesApprovalsSettings);
        
        mockMvc.perform(post(OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}",
                                                                       organization1.getId().toString()) + otherTrTypesApprovalsSettings.getTransportType() +
                             "/restore")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andReturn();
        
        settings = settingsRepository.findByOrganizationIdAndTransportType(organization1.getId(), "BICYCLE")
                                     .orElseThrow();
        assertThat(settings.isApprovalActive()).isTrue();
        assertThat(settings.getMinCostToBeApproved()).isZero();
        assertThat(settings.getPurposeAndRegionItems()).isEmpty();
        assertThat(settings.isTripApprovalActive()).isTrue();

        final var messageCaptor = ArgumentCaptor.forClass(OtherTrTypesApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.otherTrCheck(message, settings);
    }
    
    @DisplayName("Создание новой настройки согласования поездок на общественном транспорте без указания региона")
    @Test
    void testPublicTrApprovalsSettingsCreateWithoutRegion() throws Exception {
        NewPurposeAndRegionApprovalSettingsItemDTO newPurposeAndRegionDTO1 =
                NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                          .purposeId(purpose2.getId())
                                                          .regionId(null)
                                                          .minCostToBeApproved(250)
                                                          .build();
        
        NewOtherTrTypesApprovalsSettingsDTO
                modelWithoutRegion = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2),
                "CARSHARING", 500, true, true);
        String requestWithoutRegion = objectMapper.writeValueAsString(modelWithoutRegion);
        
        settingsRepository.deleteAll();
        assertThat(settingsRepository.count()).isZero();
        
        sendCreateRequest(requestWithoutRegion, organization2.getId(), USER2_ID)
                .andExpect(status().isOk());
        
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
    
    
    private ResultActions sendCreateRequest(String request, UUID organizationId, String user) throws Exception {
        return mockMvc.perform(
                post(OTHER_TR_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organizationId.toString()))
                        .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE));
    }
    
    private void compare(OtherTrTypesApprovalsSettings model, NewOtherTrTypesApprovalsSettingsDTO dto) {
        assertThat(model.getMinCostToBeApproved()).isEqualTo(dto.getMinCostToBeApproved());
        assertThat(model.isApprovalActive()).isEqualTo(dto.isApprovalActive());
        for (int i = 0; i < model.getPurposeAndRegionItems().size(); i++) {
            var purposeAndRegionItem = model.getPurposeAndRegionItems().get(i);
            var newPurposeAndRegionItem = dto.getPurposeAndRegionItems().get(i);
            assertThat(newPurposeAndRegionItem.getRegionId()).isEqualTo(purposeAndRegionItem.getRegion().getId());
            assertThat(newPurposeAndRegionItem.getPurposeId()).isEqualTo(purposeAndRegionItem.getTripPurpose().getId());
            assertThat(newPurposeAndRegionItem.getMinCostToBeApproved())
                    .isEqualTo(purposeAndRegionItem.getMinCostToBeApproved());
        }
        assertThat(model.isTripApprovalActive()).isEqualTo(dto.isTripApprovalActive());
    }
    
    private void compare(OtherTrTypesApprovalsSettings model1, OtherTrTypesApprovalsSettings model2) {
        assertThat(model1.getMinCostToBeApproved()).isEqualTo(model2.getMinCostToBeApproved());
        assertThat(model1.isApprovalActive()).isEqualTo(model2.isApprovalActive());
        for (int i = 0; i < model1.getPurposeAndRegionItems().size(); i++) {
            var purposeAndRegionItem = model1.getPurposeAndRegionItems().get(i);
            var newPurposeAndRegionItem = model2.getPurposeAndRegionItems().get(i);
            assertThat(newPurposeAndRegionItem.getRegion().getId()).isEqualTo(purposeAndRegionItem.getRegion().getId());
            assertThat(newPurposeAndRegionItem.getTripPurpose().getId())
                    .isEqualTo(purposeAndRegionItem.getTripPurpose().getId());
            assertThat(newPurposeAndRegionItem.getMinCostToBeApproved())
                    .isEqualTo(purposeAndRegionItem.getMinCostToBeApproved());
        }
        assertThat(model1.isTripApprovalActive()).isEqualTo(model2.isTripApprovalActive());
    }
    
    private NewOtherTrTypesApprovalsSettingsDTO createNewDTO(
            List<NewPurposeAndRegionApprovalSettingsItemDTO> items,
            String transportType,
            int minCostToBeApproved,
            boolean approvalActive,
            boolean tripApprovalActive) {
        return NewOtherTrTypesApprovalsSettingsDTO.builder()
                                              .minCostToBeApproved(minCostToBeApproved)
                                              .transportType(transportType)
                                              .purposeAndRegionItems(items)
                                              .approvalActive(approvalActive)
                                              .tripApprovalActive(tripApprovalActive)
                                              .build();
    }
}
