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
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;
import ru.sberbank.ditsib.transport.approvals.database.model.Position;
import ru.sberbank.ditsib.transport.approvals.database.model.TaxiApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewPurposeAndRegionApprovalSettingsItemDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewTaxiApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.TaxiApprovalsSettingsDTO;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

@AutoConfigureMockMvc
@SpringBootTest
@DisplayName("Проверка контроллера настроек согласования такси")
@EmbeddedPostgres
@MockitoBean(types = {JwtDecoder.class})
class TaxiApprovalsSettingsControllerTest extends ApprovalsSettingsSharedTestData {
    
    private static final String TAXI_APPROVAL_SETTINGS_BASE_URL = "/{organizationId}/settings/request/taxi/";
    private final TestSharedData sharedData = new TestSharedData();
    private TaxiApprovalsSettings taxiApprovalsSettings;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TaxiApprovalsSettingsRepository settingsRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private TripPurposeRepository purposeRepository;
    
    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean("approvalsSettingsOutput")
    private OutputBridge approvalsSettingsOutput;
    
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
    
        taxiApprovalsSettings = TaxiApprovalsSettings.builder()
                                                     .approvalActive(false)
                                                     .minCostToBeApproved(500)
                                                     .organization(organization1)
                                                     .transportType("TAXI")
                                                     .purposeAndRegionItems(Arrays.asList(item1, item2))
                                                     .build();
        settingsRepository.save(taxiApprovalsSettings);

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
    
    @DisplayName("Создание новой настройки согласования такси с некорректными данными")
    @Test
    void testTaxiApprovalsSettingsCreateWithPurposeId() throws Exception {
        NewPurposeAndRegionApprovalSettingsItemDTO newPurposeAndRegionDTO1 =
                NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                          //null - incorrect value
                                                          .purposeId(null)
                                                          .regionId(UUID.randomUUID())
                                                          .minCostToBeApproved(250)
                                                          .build();
    
        NewPurposeAndRegionApprovalSettingsItemDTO newPurposeAndRegionDTO2 =
                NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                          .purposeId(purpose2.getId())
                                                          //null - incorrect value
                                                          .regionId(null)
                                                          .minCostToBeApproved(250)
                                                          .build();
        
        NewTaxiApprovalsSettingsDTO modelWithIncorrectPurpose =
                createNewDTO(Collections.singletonList(newPurposeAndRegionDTO1), 500);
        
        String requestWithIncorrectPurpose = objectMapper.writeValueAsString(modelWithIncorrectPurpose);
    
        NewTaxiApprovalsSettingsDTO modelWithIncorrectRegion = createNewDTO(Collections.singletonList(newPurposeAndRegionDTO2), 500);
        
        objectMapper.writeValueAsString(modelWithIncorrectRegion);
    
        NewTaxiApprovalsSettingsDTO modelWithIncorrectItems = createNewDTO(null, 500);

        String requestWithIncorrectItems = objectMapper.writeValueAsString(modelWithIncorrectItems);
        
        assertThat(settingsRepository.count()).isEqualTo(1);
    
        sendCreateRequest(USER2_ID, requestWithIncorrectPurpose, organization2.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("purposeAndRegionItems[0].purposeId"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"));
    
        // доп. настроек по целям поездки может и не быть
        sendCreateRequest(USER2_ID, requestWithIncorrectItems, organization2.getId())
                .andExpect(status().isOk());
        
        assertThat(settingsRepository.count()).isEqualTo(2);
    }
    
    @DisplayName("Создание новой настройки согласования такси для организации, у которой настройка уже есть")
    @Test
    void testAlreadyExistingTaxiApprovalsSettingsCreate() throws Exception {
        NewTaxiApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 500);
        
        String request = objectMapper.writeValueAsString(value);
        
        assertThat(settingsRepository.count()).isEqualTo(1);
        
        sendCreateRequest(USER1_ID, request, organization1.getId()).andExpect(status().isConflict()).andReturn();
        
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
    
    @DisplayName("Создание новой настройки согласования такси")
    @Test
    void testTaxiApprovalsSettingsCreate() throws Exception {
        NewTaxiApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 500);
        String request = objectMapper.writeValueAsString(value);
        sendCreateRequest(USER2_ID, request, organization2.getId()).andExpect(status().isOk()).andReturn();
        
        TaxiApprovalsSettings settings = settingsRepository
                .findByOrganizationId(organization2.getId()).orElseThrow();
        compare(settings, value);

        final var messageCaptor = ArgumentCaptor.forClass(ApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.commonCheck(message, settings);
    }
    
    @DisplayName("Чтение несуществующей настройки согласования такси (создаётся новая дефолтная настройка)")
    @Test
    void testNonExistingTaxiApprovalsSettingsGet() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        mockMvc.perform(
                get(TAXI_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization2.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID).claim("roles", "ROLE_USER"))))
                                  .andExpect(status().isOk())
                                  .andReturn();
    }
    
    @DisplayName("Чтение настройки согласования такси")
    @Test
    void testTaxiApprovalsSettingsGet() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        MvcResult result = mockMvc.perform(
                get(TAXI_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER"))))
               .andExpect(status().isOk())
               .andReturn();
        TaxiApprovalsSettingsDTO actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                 new TypeReference<>() {});

        assertThat(actual.getTransportType()).isEqualTo(taxiApprovalsSettings.getTransportType());
        assertThat(actual.getOrganizationId()).isEqualTo(taxiApprovalsSettings.getOrganization().getId());
        for (int i = 0; i < actual.getPurposeAndRegionItems().size(); i++) {
            var actualItem = actual.getPurposeAndRegionItems().get(i);
            var expectedItem =
                    taxiApprovalsSettings.getPurposeAndRegionItems().get(i);
            assertThat(expectedItem.getRegion().getId()).isEqualTo(actualItem.getRegion().getId());
            assertThat(expectedItem.getRegion().getName()).isEqualTo(actualItem.getRegion().getName());
            assertThat(expectedItem.getTripPurpose().getId()).isEqualTo(actualItem.getTripPurpose().getId());
            assertThat(expectedItem.getTripPurpose().getLabel()).isEqualTo(actualItem.getTripPurpose().getLabel());
            assertThat(expectedItem.getMinCostToBeApproved()).isEqualTo(actualItem.getMinCostToBeApproved());
        }
        assertThat(actual.getMinCostToBeApproved()).isEqualTo(taxiApprovalsSettings.getMinCostToBeApproved());
    }
    
    @DisplayName("Изменение несуществующей настройки согласования такси")
    @Test
    void testNotExistingTaxiApprovalsSettingsUpdate() throws Exception {
        NewTaxiApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 250);
        String request = objectMapper.writeValueAsString(value);
        mockMvc.perform(
                put(TAXI_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString()) + UUID.randomUUID())
                        .content(request)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isNotFound()).andReturn();
        
        TaxiApprovalsSettings settings =
                settingsRepository.findByOrganizationId(organization1.getId())
                                                           .orElseThrow();
        compare(settings, taxiApprovalsSettings);
    }
    
    @DisplayName("Изменение настройки согласования такси")
    @Test
    void testTaxiApprovalsSettingsUpdate() throws Exception {
        NewTaxiApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 250);
        
        String request = objectMapper.writeValueAsString(value);
        TaxiApprovalsSettings savedSettings = settingsRepository.findByOrganizationId(organization1.getId()).orElseThrow();
        mockMvc.perform(
                put(TAXI_APPROVAL_SETTINGS_BASE_URL
                            .replace("{organizationId}", organization1.getId().toString()) + savedSettings.getId())
                        .content(request)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
    
        TaxiApprovalsSettings settings =
                settingsRepository.findByOrganizationId(organization1.getId())
                                                           .orElseThrow();
        compare(settings, value);
        final var messageCaptor = ArgumentCaptor.forClass(ApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.commonCheck(message, settings);
    }
    
    @DisplayName("Удаление настройки согласования такси")
    @Test
    void testTaxiApprovalsSettingsDelete() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        TaxiApprovalsSettings savedSettings = settingsRepository.findByOrganizationId(organization1.getId()).orElseThrow();
        final UUID settingsId = savedSettings.getId();
        
        mockMvc.perform(delete(TAXI_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}",
                                                                       organization1.getId().toString()) + settingsId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
               .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andReturn();
        assertThat(settingsRepository.count()).isZero();

        final var messageCaptor = ArgumentCaptor.forClass(ApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, savedSettings.getId())));
        final var message = messageCaptor.getValue();
        assertThat(message.getId()).isEqualTo(settingsId);
        assertThat(message.getTransportType()).isEqualTo(TAXI.name());
        assertThat(message.isDeleted()).isTrue();
    }
    
    @DisplayName("Сброс несуществующей настройки для такси")
    @Test
    void testNonExistingTaxiApprovalsSettingsRestore() throws Exception {
        TaxiApprovalsSettings settings =
                settingsRepository.findByOrganizationId(organization1.getId())
                                                           .orElseThrow();
        
        assertThat(settings.isApprovalActive()).isFalse();
        assertThat(settings.getMinCostToBeApproved()).isEqualTo(500);
        
        mockMvc.perform(post(
                TAXI_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString()) + UUID.randomUUID() +
                "/restore")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isNotFound())
               .andReturn();
    }
    
    @DisplayName("Возврат настройки для такси по умолчанию")
    @Test
    void testTaxiApprovalsSettingsRestore() throws Exception {
        TaxiApprovalsSettings settings =
                settingsRepository.findByOrganizationId(organization1.getId())
                                                           .orElseThrow();
        compare(settings, taxiApprovalsSettings);
        
        TaxiApprovalsSettings savedSettings = settingsRepository.findByOrganizationId(organization1.getId()).orElseThrow();
        mockMvc.perform(post(TAXI_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}",
                                                                     organization1.getId().toString()) + savedSettings.getId() + "/restore")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andReturn();
    
        settings = settingsRepository.findByOrganizationId(organization1.getId())
                                                           .orElseThrow();
        assertThat(settings.isApprovalActive()).isTrue();
        assertThat(settings.getMinCostToBeApproved()).isZero();
        assertThat(settings.getPurposeAndRegionItems()).isEmpty();

        final var messageCaptor = ArgumentCaptor.forClass(ApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.commonCheck(message, settings);
    }
    
    @DisplayName("Создание новой настройки согласования поездок на общественном такси без указания региона")
    @Test
    void testPublicTrApprovalsSettingsCreateWithoutRegion() throws Exception {
        NewPurposeAndRegionApprovalSettingsItemDTO newPurposeAndRegionDTO1 =
                NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                          .purposeId(purpose2.getId())
                                                          .regionId(null)
                                                          .minCostToBeApproved(250)
                                                          .build();
        
        NewTaxiApprovalsSettingsDTO
                modelWithoutRegion = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 500);
        String requestWithoutRegion = objectMapper.writeValueAsString(modelWithoutRegion);
        
        settingsRepository.deleteAll();
        assertThat(settingsRepository.count()).isZero();
        
        sendCreateRequest(USER2_ID, requestWithoutRegion, organization2.getId())
                .andExpect(status().isOk());
        
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
    
    private ResultActions sendCreateRequest(String userId, String request, UUID organizationId) throws Exception {
        return mockMvc.perform(
                post(TAXI_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organizationId.toString()))
                        .with(jwt().jwt(builder -> builder.jti(userId).claim("roles", "ROLE_USER")))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE));
    }
    
    private void compare(TaxiApprovalsSettings model, NewTaxiApprovalsSettingsDTO dto) {
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
    }
    
    private void compare(TaxiApprovalsSettings model1, TaxiApprovalsSettings model2) {
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
    }
    
    private NewTaxiApprovalsSettingsDTO createNewDTO(
            List<NewPurposeAndRegionApprovalSettingsItemDTO> items,
            int minCostToBeApproved) {
        return NewTaxiApprovalsSettingsDTO.builder()
                                              .minCostToBeApproved(minCostToBeApproved)
                                              .purposeAndRegionItems(items)
                                              .approvalActive(true)
                                              .build();
    }
}
