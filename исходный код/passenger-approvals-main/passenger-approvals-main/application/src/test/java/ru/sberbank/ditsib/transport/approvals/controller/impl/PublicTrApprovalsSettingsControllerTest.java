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
import ru.sber.transport.approvals.messaging.PublicApprovalsSettingsMessage;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.dao.*;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;
import ru.sberbank.ditsib.transport.approvals.database.model.Position;
import ru.sberbank.ditsib.transport.approvals.database.model.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewPublicTrApprovalsSettingsDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewPurposeAndRegionApprovalSettingsItemDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.PublicTrApprovalsSettingsDTO;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.PUBLIC;

@AutoConfigureMockMvc
@SpringBootTest
@DisplayName("Проверка контроллера настроек согласования поездок на общественном транспорте")
@EmbeddedPostgres
@MockitoBean(types = {JwtDecoder.class})
class PublicTrApprovalsSettingsControllerTest extends ApprovalsSettingsSharedTestData {
    
    private static final String PUBLIC_APPROVAL_SETTINGS_BASE_URL = "/{organizationId}/settings/request/public/";
    private final TestSharedData sharedData = new TestSharedData();
    private PublicTrApprovalsSettings publicTrApprovalsSettings;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PublicTrApprovalsSettingsRepository settingsRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private TripPurposeRepository purposeRepository;

    @MockitoBean("approvalsSettingsOutput")
    private OutputBridge approvalsSettingsOutput;

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
        
        publicTrApprovalsSettings = PublicTrApprovalsSettings.builder()
                                                             .approvalActive(false)
                                                             .minCostToBeApproved(500)
                                                             .organization(organization1)
                                                             .transportType("PUBLIC")
                                                             .purposeAndRegionItems(Arrays.asList(item1, item2))
                                                             .approvalDocumentCheck(false)
                                                             .affirmativeActive(false)
                                                             .tripConfirmationActive(false)
                                                             .tripConfirmationDocumentCheck(false)
                                                             .build();
        settingsRepository.save(publicTrApprovalsSettings);

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
    
    @DisplayName("Создание новой настройки согласования поездок на общественном транспорте с некорректными данными")
    @Test
    void testPublicTrApprovalsSettingsCreateWithPurposeId() throws Exception {
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
        
        NewPublicTrApprovalsSettingsDTO modelWithIncorrectPurpose = createNewDTO(Collections.singletonList(newPurposeAndRegionDTO), 500, true, true, true);
        String requestWithIncorrectPurpose = objectMapper.writeValueAsString(modelWithIncorrectPurpose);
        
        NewPublicTrApprovalsSettingsDTO modelWithIncorrectRegion = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 500, true, true, true);
        objectMapper.writeValueAsString(modelWithIncorrectRegion);
        
        NewPublicTrApprovalsSettingsDTO modelWithIncorrectItems = createNewDTO(null, 500, true, true, true);

        objectMapper.writeValueAsString(modelWithIncorrectItems);
        
        assertThat(settingsRepository.count()).isEqualTo(1);
        
        sendCreateRequest(USER1_ID, requestWithIncorrectPurpose, organization1.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("purposeAndRegionItems[0].purposeId"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"));
    
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
    
    @DisplayName("Создание новой настройки согласования поездок на общественном транспорте с некорректными данными. " +
                 "Нет настроек")
    @Test
    void testPublicTrApprovalsSettingsCreateWithPurposeId_noSettings() throws Exception {
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
        
        NewPublicTrApprovalsSettingsDTO modelWithIncorrectPurpose = createNewDTO(Collections.singletonList(newPurposeAndRegionDTO), 500, true, true, true);
        objectMapper.writeValueAsString(modelWithIncorrectPurpose);
        
        NewPublicTrApprovalsSettingsDTO modelWithIncorrectRegion = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 500, true, true, true);
        objectMapper.writeValueAsString(modelWithIncorrectRegion);
        
        NewPublicTrApprovalsSettingsDTO modelWithIncorrectItems = createNewDTO(null, 500, true, true, true);

        String requestWithIncorrectItems = objectMapper.writeValueAsString(modelWithIncorrectItems);
        
        assertThat(settingsRepository.count()).isEqualTo(1);
    
        // доп. настроек по целям поездки может и не быть
        sendCreateRequest(USER2_ID, requestWithIncorrectItems, organization2.getId())
                .andExpect(status().isOk());
    
        assertThat(settingsRepository.count()).isEqualTo(2);
    }
    
    @DisplayName("Создание новой настройки согласования поездок на общественном транспорте для организации, у которой" +
                 " настройка уже есть")
    @Test
    void testAlreadyExistingPublicTrApprovalsSettingsCreate() throws Exception {
        NewPublicTrApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 500, true, true, true);
        String request = objectMapper.writeValueAsString(value);
        
        assertThat(settingsRepository.count()).isEqualTo(1);
        
        sendCreateRequest(USER1_ID, request, organization1.getId()).andExpect(status().isConflict()).andReturn();
        
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
    
    @DisplayName("Создание новой настройки согласования поездок на общественном транспорте")
    @Test
    void testPublicTrApprovalsSettingsCreate() throws Exception {
        NewPublicTrApprovalsSettingsDTO newDto = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 500, false, false, false);
        String request = objectMapper.writeValueAsString(newDto);
        sendCreateRequest(USER2_ID, request, organization2.getId()).andExpect(status().isOk()).andReturn();
        
        PublicTrApprovalsSettings settings =
                settingsRepository.findByOrganizationId(organization2.getId())
                                  .orElseThrow();
        compare(settings, newDto);

        final var messageCaptor = ArgumentCaptor.forClass(PublicApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.publicCheck(message, settings);
    }
    
    
    @DisplayName("Чтение несуществующей настройки согласования поездок на общественном транспорте " +
                 "(создаётся новая дефолтная настройка)")
    @Test
    void testNonExistingPublicTrApprovalsSettingsGet() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        mockMvc.perform(
                get(PUBLIC_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization2.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID).claim("roles", "ROLE_USER"))))
               .andExpect(status().isOk())
               .andReturn();
    }
    
    @DisplayName("Чтение настройки согласования поездок на общественном транспорте")
    @Test
    void testPublicTrApprovalsSettingsGet() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        MvcResult result = mockMvc.perform(
                get(PUBLIC_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER"))))
                                  .andExpect(status().isOk())
                                  .andReturn();
        PublicTrApprovalsSettingsDTO actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                 new TypeReference<>() {});
        
        assertThat(actual.getTransportType()).isEqualTo(publicTrApprovalsSettings.getTransportType());
        assertThat(actual.getOrganizationId()).isEqualTo(publicTrApprovalsSettings.getOrganization().getId());
        assertThat(actual.getMinCostToBeApproved()).isEqualTo(publicTrApprovalsSettings.getMinCostToBeApproved());
        for (int i = 0; i < actual.getPurposeAndRegionItems().size(); i++) {
            var actualItem = actual.getPurposeAndRegionItems().get(i);
            var expectedItem = publicTrApprovalsSettings.getPurposeAndRegionItems().get(i);
            assertThat(expectedItem.getRegion().getId()).isEqualTo(actualItem.getRegion().getId());
            assertThat(expectedItem.getRegion().getName()).isEqualTo(actualItem.getRegion().getName());
            assertThat(expectedItem.getTripPurpose().getId()).isEqualTo(actualItem.getTripPurpose().getId());
            assertThat(expectedItem.getTripPurpose().getLabel()).isEqualTo(actualItem.getTripPurpose().getLabel());
            assertThat(expectedItem.getMinCostToBeApproved()).isEqualTo(actualItem.getMinCostToBeApproved());
        }
        assertThat(actual.isApprovalDocumentCheck()).isEqualTo(publicTrApprovalsSettings.isApprovalDocumentCheck());
        assertThat(actual.isAffirmativeActive()).isEqualTo(publicTrApprovalsSettings.isAffirmativeActive());
        assertThat(actual.isTripConfirmationActive()).isEqualTo(publicTrApprovalsSettings.isTripConfirmationActive());
        assertThat(actual.isTripConfirmationDocumentCheck()).isEqualTo(publicTrApprovalsSettings.isTripConfirmationDocumentCheck());
    }
    
    @DisplayName("Изменение несуществующей настройки согласования поездок на общественном транспорте")
    @Test
    void testNotExistingPublicTrApprovalsSettingsUpdate() throws Exception {
        NewPublicTrApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 250, true, true, true);
        String request = objectMapper.writeValueAsString(value);
        mockMvc.perform(
                put(PUBLIC_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString()) +
                    UUID.randomUUID())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isNotFound()).andReturn();
        
        PublicTrApprovalsSettings settings =
                settingsRepository.findByOrganizationId(organization1.getId())
                                  .orElseThrow();
        compare(settings, publicTrApprovalsSettings);
    }
    
    @DisplayName("Изменение настройки согласования поездок на общественном транспорте")
    @Test
    void testPublicTrApprovalsSettingsUpdate() throws Exception {
        NewPublicTrApprovalsSettingsDTO value = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 250, false, false, false);
        String request = objectMapper.writeValueAsString(value);
        PublicTrApprovalsSettings savedSettings = this.settingsRepository.findByOrganizationId(organization1.getId()).orElseThrow();
        mockMvc.perform(
                put(PUBLIC_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organization1.getId().toString()) +
                    savedSettings.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        
        PublicTrApprovalsSettings settings =
                settingsRepository.findByOrganizationId(organization1.getId())
                                  .orElseThrow();
        compare(settings, value);

        final var messageCaptor = ArgumentCaptor.forClass(PublicApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.publicCheck(message, settings);
    }
    
    
    @DisplayName("Удаление настройки согласования поездок на общественном транспорте")
    @Test
    void testPublicTrApprovalsSettingsDelete() throws Exception {
        assertThat(settingsRepository.count()).isEqualTo(1);
        PublicTrApprovalsSettings savedSetting = settingsRepository.findByOrganizationId(organization1.getId()).orElseThrow();
        final UUID settingsId = savedSetting.getId();
        
        mockMvc.perform(delete(PUBLIC_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}",
                                                                         organization1.getId().toString()) + settingsId)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andReturn();
        assertThat(settingsRepository.count()).isZero();

        final var messageCaptor = ArgumentCaptor.forClass(ApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settingsId)));
        final var message = messageCaptor.getValue();
        assertThat(message.getId()).isEqualTo(settingsId);
        assertThat(message.getTransportType()).isEqualTo(PUBLIC.name());
        assertThat(message.isDeleted()).isTrue();
    }
    
    @DisplayName("Сброс настройки согласования поездок на общественном транспорте")
    @Test
    void testPublicTrApprovalsSettingsRestore() throws Exception {
        PublicTrApprovalsSettings settings =
                settingsRepository.findByOrganizationId(organization1.getId())
                                  .orElseThrow();
        
        compare(settings, publicTrApprovalsSettings);
        
        mockMvc.perform(post(PUBLIC_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}",
                                                                       organization1.getId().toString()) +
                             settings.getId() + "/restore")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andReturn();
        
        settings = settingsRepository.findByOrganizationId(organization1.getId()).orElseThrow();
        assertThat(settings.isApprovalActive()).isTrue();
        assertThat(settings.getMinCostToBeApproved()).isZero();
        assertThat(settings.getPurposeAndRegionItems()).isEmpty();
        assertThat(settings.isApprovalDocumentCheck()).isTrue();
        assertThat(settings.isAffirmativeActive()).isTrue();
        assertThat(settings.isTripConfirmationActive()).isTrue();
        assertThat(settings.isTripConfirmationDocumentCheck()).isTrue();

        final var messageCaptor = ArgumentCaptor.forClass(PublicApprovalsSettingsMessage.class);
        verify(approvalsSettingsOutput).send(messageCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, settings.getId())));
        final var message = messageCaptor.getValue();
        sharedData.publicCheck(message, settings);
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
        
        NewPublicTrApprovalsSettingsDTO modelWithoutRegion = createNewDTO(Arrays.asList(newPurposeAndRegionDTO1, newPurposeAndRegionDTO2), 500, true, true, true);
        String requestWithoutRegion = objectMapper.writeValueAsString(modelWithoutRegion);
        
        settingsRepository.deleteAll();
        assertThat(settingsRepository.count()).isZero();
        
        sendCreateRequest(USER2_ID, requestWithoutRegion, organization2.getId())
                .andExpect(status().isOk());
        
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
    
    private ResultActions sendCreateRequest(String userId, String request, UUID organizationId) throws Exception {
        return mockMvc.perform(
                post(PUBLIC_APPROVAL_SETTINGS_BASE_URL.replace("{organizationId}", organizationId.toString()))
                        .with(jwt().jwt(builder -> builder.jti(userId).claim("roles", "ROLE_USER")))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE));
    }
    
    private void compare(PublicTrApprovalsSettings model, NewPublicTrApprovalsSettingsDTO dto) {
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
        assertThat(model.isApprovalDocumentCheck()).isEqualTo(dto.isApprovalDocumentCheck());
        assertThat(model.isAffirmativeActive()).isEqualTo(dto.isAffirmativeActive());
        assertThat(model.isTripConfirmationActive()).isEqualTo(dto.isTripConfirmationActive());
        assertThat(model.isTripConfirmationDocumentCheck()).isEqualTo(dto.isTripConfirmationDocumentCheck());
    }
    
    private void compare(PublicTrApprovalsSettings model1, PublicTrApprovalsSettings model2) {
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
        assertThat(model1.isApprovalDocumentCheck()).isEqualTo(model2.isApprovalDocumentCheck());
        assertThat(model1.isAffirmativeActive()).isEqualTo(model2.isAffirmativeActive());
        assertThat(model1.isTripConfirmationActive()).isEqualTo(model2.isTripConfirmationActive());
        assertThat(model1.isTripConfirmationDocumentCheck())
                .isEqualTo(model2.isTripConfirmationDocumentCheck());
    }
    
    private NewPublicTrApprovalsSettingsDTO createNewDTO(
            List<NewPurposeAndRegionApprovalSettingsItemDTO> items,
            int minCostToBeApproved,
            boolean affirmativeActive,
            boolean tripConfirmationActive,
            boolean tripConfirmationDocumentCheck) {
        return NewPublicTrApprovalsSettingsDTO.builder()
                                              .minCostToBeApproved(minCostToBeApproved)
                                              .purposeAndRegionItems(items)
                                              .approvalActive(true)
                                              .affirmativeActive(affirmativeActive)
                                              .tripConfirmationActive(tripConfirmationActive)
                                              .tripConfirmationDocumentCheck(tripConfirmationDocumentCheck)
                                              .build();
    }
}
