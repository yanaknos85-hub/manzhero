package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsCreateDTO;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsUpdateDTO;
import ru.sberbank.ditsib.corpclient.dto.mapper.SettingsItemDTOMapper;
import ru.sberbank.ditsib.corpclient.dto.mapper.SettingsItemDTOMapperImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@DisplayName("Проверка контроллера настроек совместных поездок")
@ActiveProfiles("test")
class SharedRideSettingControllerTest {
    private final SettingsItemDTOMapper mapper = new SettingsItemDTOMapperImpl();

    public static final String USER1_ID_STRING = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    private static String url;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private AttributeRepository attributeRepository;
    @Autowired
    private SharedRideSettingsRepository settingsRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    void initialize() {
        Organization organization = new Organization();
        organization.setAddress("Address");
        organization.setOfficialName("Organization");
        organization.setMsrn("msrn");
        organization.setTid("tin");

        Department department = Department.builder()
                .name("Department")
                .code("DPT1")
                .organization(organizationRepository.save(organization))
                .humanReadableId("US-0001-11")
                .updateTime(OffsetDateTime.now())
                .build();

        //Общая должность - Должность3 (для проверки конфликта)
        Position position1 = new Position();
        position1.setName("Position1");
        position1.setOrganization(organization);

        Position position2 = new Position();
        position2.setName("Position2");
        position2.setOrganization(organization);

        Position position3 = new Position();
        position3.setName("Position3");
        position3.setOrganization(organization);

        Set<Position> positions1 = new HashSet<>();
        positions1.add(position1);
        positions1.add(position3);

        Set<Position> positions2 = new HashSet<>();
        positions2.add(position2);
        positions2.add(position3);

        //общий Признак - Признак3 (для проверки конфликта)
        Attribute attribute1 = Attribute.builder().name("Tag1").build();
        Attribute attribute2 = Attribute.builder().name("Tag2").build();
        Attribute attribute3 = Attribute.builder().name("Tag3").build();

        Set<Attribute> attributes1 = new HashSet<>();
        Set<Attribute> attributes2 = new HashSet<>();
        attributes1.add(attribute1);
        attributes1.add(attribute3);
        attributes2.add(attribute2);
        attributes2.add(attribute3);

        position1.setHumanReadableId("PS-001-1");
        position2.setHumanReadableId("PS-001-2");
        position3.setHumanReadableId("PS-001-3");
        position1 = positionRepository.save(position1);
        position2 = positionRepository.save(position2);
        position3 = positionRepository.save(position3);
        attributeRepository.save(attribute1);
        attributeRepository.save(attribute2);
        attributeRepository.save(attribute3);
        organization = organizationRepository.save(organization);
        department = departmentRepository.save(department);

        //Общий сотрудник - Сотрудник3 (для проверки конфликта)
        var id1 = UUID.fromString(USER1_ID_STRING);
        Employee employee1 = Employee.builder()
                .id(id1)
                .position(positionRepository.save(position1))
                .department(departmentRepository.save(department))
                .userId(id1)
                .firstName("Name1")
                .lastName("Surname1")
                .patronymic("Patronymic1")
                .personnelNumber("personnel1")
                .attributes(attributes1)
                .humanReadableId("US-0001-1")
                .email("email1@mail.ru")
                .updateTime(OffsetDateTime.now())
                .organization(organizationRepository.save(department.getOrganization()))
                .build();

        var id2 = UUID.randomUUID();
        Employee employee2 = Employee.builder()
                .id(id2)
                .position(positionRepository.save(position2))
                .department(departmentRepository.save(department))
                .userId(id2)
                .firstName("Name2")
                .lastName("Surname2")
                .patronymic("Patronymic2")
                .personnelNumber("personnel2")
                .attributes(attributes2)
                .humanReadableId("US-0002-1")
                .organization(organizationRepository.save(department.getOrganization()))
                .email("email2@mail.ru")
                .organization(department.getOrganization())
                .updateTime(OffsetDateTime.now())
                .build();

        var id3 = UUID.randomUUID();
        Employee employee3 = Employee.builder()
                .id(id3)
                .position(positionRepository.save(position3))
                .department(departmentRepository.save(department))
                .userId(id3)
                .firstName("Name3")
                .lastName("Surname3")
                .patronymic("Patronymic3")
                .personnelNumber("personnel3")
                .attributes(attributes2)
                .humanReadableId("US-0002-2")
                .email("email3@mail.ru")
                .organization(organizationRepository.save(department.getOrganization()))
                .updateTime(OffsetDateTime.now())
                .build();

        employee1 = employeeRepository.save(employee1);
        employee2 = employeeRepository.save(employee2);
        employee3 = employeeRepository.save(employee3);

        Set<Employee> employees1 = new HashSet<>();
        employees1.add(employee1);
        employees1.add(employee3);

        Set<Employee> employees2 = new HashSet<>();
        employees2.add(employee2);
        employees2.add(employee3);

        SharedRideSettingsItem settingItem1 = SharedRideSettingsItem.builder()
                .positions(positions1)
                .attributes(attributes1)
                .employees(employees1)
                .build();

        SharedRideSettingsItem settingItem2 = SharedRideSettingsItem.builder()
                .positions(positions2)
                .attributes(attributes2)
                .employees(employees2)
                .build();

        SharedRideSettingsItem settingItem3 = SharedRideSettingsItem.builder()
                .positions(positions1)
                .attributes(attributes2)
                .employees(employees1)
                .build();

        Map<SharedRideSettingType, SharedRideSettingsItem> settingsMap = new HashMap<>(3);
        settingsMap.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE, settingItem1);
        settingsMap.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY, settingItem2);
        settingsMap.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE, settingItem3);

        SharedRideSettings settings = SharedRideSettings.builder()
                .organization(organization)
                .transportType(TransportTypeEnum.TAXI)
                .settings(settingsMap)
                .economyIndicationYellowRangeLowerBorder(25)
                .economyIndicationYellowRangeUpperBorder(65)
                .build();
        settingsRepository.save(settings);

        //проинициализируем url
        url = "/" + organization.getId() + "/shared_ride_settings";
    }

    private void initializeSecondSettings() {
        Organization organization = organizationRepository.findAll().getFirst();
        Set<Position> positions = new HashSet<>(positionRepository.findAll());
        Set<Attribute> attributes = new HashSet<>(attributeRepository.findAll());
        Set<Employee> employees = new HashSet<>(employeeRepository.findAll());

        SharedRideSettingsItem settingItem1 = SharedRideSettingsItem.builder()
                .positions(positions)
                .attributes(attributes)
                .employees(employees)
                .build();

        SharedRideSettingsItem settingItem2 = SharedRideSettingsItem.builder()
                .positions(positions)
                .attributes(attributes)
                .employees(employees)
                .build();

        SharedRideSettingsItem settingItem3 = SharedRideSettingsItem.builder()
                .positions(positions)
                .attributes(attributes)
                .employees(employees)
                .build();

        Map<SharedRideSettingType, SharedRideSettingsItem> settingsMap = new HashMap<>(3);
        settingsMap.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE, settingItem1);
        settingsMap.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY, settingItem2);
        settingsMap.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE, settingItem3);

        SharedRideSettings settings = SharedRideSettings.builder()
                .organization(organization)
                .transportType(TransportTypeEnum.PERSONAL)
                .settings(settingsMap)
                .economyIndicationYellowRangeLowerBorder(35)
                .economyIndicationYellowRangeUpperBorder(75)
                .build();
        settingsRepository.save(settings);
    }

    @Test
    @DisplayName("Получение всех настроек для организации - успех")
    void test_getAll_success() throws Exception {
        var result = mockMvc.perform(get(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();

        var map = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<List<Map<String, Object>>>() {
                }
        ).getFirst();

        var expected = settingsRepository.findAll().getFirst();

        assertThat(map).containsEntry("transportType", expected.getTransportType().name())
                .containsEntry("organizationId", expected.getOrganization().getId().toString())
                .containsEntry("id", expected.getId().toString())
                .containsEntry("economyIndicationYellowRangeLowerBorder", expected.getEconomyIndicationYellowRangeLowerBorder());

        var allSettings = (LinkedHashMap<String, Object>) map.get("settings");
        var singleSetting =
                (LinkedHashMap<String, Object>) allSettings.get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY.name());
        var allPositions = (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("positions");
        assertThat(allPositions).hasSize(2);
    }

    @Test
    @DisplayName("Получение конкретной настройки для организации - успех")
    void test_getAllSettingsTypes_success() throws Exception {
        //дозапишем в url id настройки
        url += "/" + settingsRepository.findAll().getFirst().getId();

        var result = mockMvc.perform(get(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();

        var map = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<Map<String, Object>>() {
                }
        );

        var expected = settingsRepository.findAll().getFirst();

        assertThat(map).containsEntry("id", expected.getId().toString())
                .containsEntry("transportType", expected.getTransportType().name())
                .containsEntry("organizationId", expected.getOrganization().getId().toString())
                .containsEntry("economyIndicationYellowRangeUpperBorder", expected.getEconomyIndicationYellowRangeUpperBorder())
        ;
        var allSettings = (LinkedHashMap<String, Object>) map.get("settings");
        var singleSetting = (LinkedHashMap<String, Object>)
                allSettings.get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE.name());
        var allPositions =
                (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("employees");
        assertThat(allPositions).hasSize(2);
    }

    private Map<String, Object> getResponseAfterSettingsCreate(SharedRideSettings newSettings) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        SharedRideSettingsCreateDTO createDTO = mapper.sharedRideSettingsToCreateDto(newSettings);
        var request = objectMapper.writeValueAsString(createDTO);

        var response = mockMvc.perform(
                        post(url).contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request)
                )
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
    }

    @Test
    @DisplayName("Добавление настройки - успех. Проверка разрешения конфликтов")
    void test_add_success_firstKindConflictsResolve() throws Exception {
        //создадим новую настройку
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        SharedRideSettings newSettings = SharedRideSettings.builder()
                .settings(settingsFromDb.getSettings())
                .organization(settingsFromDb.getOrganization())
                .economyIndicationYellowRangeLowerBorder(26)
                .economyIndicationYellowRangeUpperBorder(64)
                .transportType(TransportTypeEnum.PERSONAL)
                .build();

        var result = getResponseAfterSettingsCreate(newSettings);

        assertThat(result).isNotNull()
                .containsKey("id")
                .containsEntry("organizationId", newSettings.getOrganization().getId().toString())
                .containsEntry("transportType", newSettings.getTransportType().name())
                .containsEntry("economyIndicationYellowRangeUpperBorder", newSettings.getEconomyIndicationYellowRangeUpperBorder())
        ;

        var allSettings = (LinkedHashMap<String, Object>) result.get("settings");
        var singleSetting = (LinkedHashMap<String, Object>)
                allSettings.get(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE.name());
        var allAttributes = (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("attributes");
        assertThat(allAttributes).hasSize(2);

        /*
        Проверить разрешение конфликтов 1 рода. В исходных данных есть конфликт 1 рода - в списках с одинаковым
        приоритетом. Проверить, что размеры списков  в настройках 1 и 2 не равны изначальному
         */
        singleSetting = (LinkedHashMap<String, Object>)
                allSettings.get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE.name());
        var allEmployees =
                (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("employees");
        assertThat(allEmployees.size()).isNotEqualTo(settingsFromDb.getSettings()
                .get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE)
                .getEmployees().size());

        singleSetting = (LinkedHashMap<String, Object>)
                allSettings.get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY.name());
        var allEmployees2 =
                (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("employees");
        assertThat(allEmployees2.size()).isNotEqualTo(settingsFromDb.getSettings()
                .get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY)
                .getEmployees().size());

        assertThat(allEmployees.size()).isNotEqualTo(settingsFromDb.getSettings()
                .get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE)
                .getAttributes().size());

        assertThat(allEmployees2.size()).isNotEqualTo(settingsFromDb.getSettings()
                .get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY)
                .getAttributes().size());


        singleSetting = (LinkedHashMap<String, Object>)
                allSettings.get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE.name());
        var allPositions = (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("positions");
        assertThat(allPositions.size()).isNotEqualTo(settingsFromDb.getSettings()
                .get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE)
                .getPositions().size());

        singleSetting = (LinkedHashMap<String, Object>) allSettings.get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY.name());
        var allPositions2 = (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("positions");
        assertThat(allPositions2.size()).isNotEqualTo(settingsFromDb.getSettings()
                .get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY)
                .getPositions().size());
    }

    private Exception getResponseAfterTryToCreateIncorrectSettings(SharedRideSettings incorrect) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        SharedRideSettingsCreateDTO createDTO = mapper.sharedRideSettingsToCreateDto(incorrect);
        var request = objectMapper.writeValueAsString(createDTO);

        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(request)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResolvedException();
    }

    @Test
    @DisplayName("Добавление настройки - Исключение - Невалидные данные")
    void test_add_checkValidation() throws Exception {
        //создадим новую настройку для организации с некорректными данными - невалидные данные employee1
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        Employee employee = settingsFromDb.getSettings()
                .get(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE)
                .getEmployees().stream()
                .filter(e -> e.getFirstName().equals("Name1")).findFirst().orElseThrow();
        employee.setPersonnelNumber("");
        employee.setLastName("");

        SharedRideSettings incorrect = SharedRideSettings.builder()
                .settings(settingsFromDb.getSettings())
                .transportType(TransportTypeEnum.PERSONAL)
                .organization(settingsFromDb.getOrganization())
                .economyIndicationYellowRangeLowerBorder(10)
                .economyIndicationYellowRangeUpperBorder(90)
                .build();

        var exception = getResponseAfterTryToCreateIncorrectSettings(incorrect);

        assertThat(exception).isNotNull();
        assertThat(exception.getClass()).isEqualTo(MethodArgumentNotValidException.class);
    }

    @Test
    @DisplayName("Добавление настройки - Исключение - Неуникальный ключ \"Организация - Тип транспорта\"")
    void test_add_nonUniqueConstraintCheck() throws Exception {
        //создадим новую настройку для организации с некорректными данными - неуникальный ключ
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        SharedRideSettings nonUnique = SharedRideSettings.builder()
                .settings(settingsFromDb.getSettings())
                .organization(settingsFromDb.getOrganization())
                .transportType(TransportTypeEnum.TAXI)
                .economyIndicationYellowRangeLowerBorder(10)
                .economyIndicationYellowRangeUpperBorder(90)
                .build();

        var exception = getResponseAfterTryToCreateIncorrectSettings(nonUnique);

        assertThat(exception).isNotNull();
        assertThat(exception.getClass()).isEqualTo(DuplicateDataException.class);
    }

    private Map<String, Object> getResponseAfterSettingsUpdate(SharedRideSettings updatedSettings) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        SharedRideSettingsUpdateDTO updateDTO = mapper.sharedRideSettingsToUpdateDto(updatedSettings);
        var request = objectMapper.writeValueAsString(updateDTO);

        var response = mockMvc.perform(
                        put(url).contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request)
                )
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
    }

    @Test
    @DisplayName("Редактирование настройки - успех")
    void update_success() throws Exception {
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        Set<Employee> employees = settingsFromDb.getSettings()
                .get(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE)
                .getEmployees();
        final int size = employees.size();
        employees.clear();

        var result = getResponseAfterSettingsUpdate(settingsFromDb);

        var allSettings = (LinkedHashMap<String, Object>) result.get("settings");
        var singleSetting = (LinkedHashMap<String, Object>)
                allSettings.get(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE.name());
        var allEmployees =
                (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("employees");
        assertThat(allEmployees.size()).isNotEqualTo(size);
    }

    private Exception getResponseAfterTryToUpdateIncorrectSettings(SharedRideSettings incorrect) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        SharedRideSettingsUpdateDTO updateDTO = mapper.sharedRideSettingsToUpdateDto(incorrect);
        var request = objectMapper.writeValueAsString(updateDTO);

        return mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(request)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResolvedException();
    }

    @Test
    @DisplayName("Редактирование настройки - Исключение - Невалидные данные")
    void update_validationCheck() throws Exception {
        //испортим данные
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        Employee employee = settingsFromDb.getSettings().get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE)
                .getEmployees().stream().findFirst().orElseThrow();
        employee.setFirstName("");

        var exception = getResponseAfterTryToUpdateIncorrectSettings(settingsFromDb);

        assertThat(exception).isNotNull();
        assertThat(exception.getClass()).isEqualTo(MethodArgumentNotValidException.class);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Test
    @DisplayName("Редактирование настройки - успех - Разрешение конфликтов")
    void update_firstTypeConflictsResolveCheck() throws Exception {
        //дозапишем в настройки 1,2 employee3 и убедимся, что при сохранении он будет удален (разрешен конфликт 1 рода)
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        Employee employee3 = employeeRepository.findByPersonnelNumber("personnel3").orElseThrow();
        settingsFromDb.setTransportType(TransportTypeEnum.PERSONAL);

        var result = getResponseAfterSettingsUpdate(settingsFromDb);

        var allSettings = (LinkedHashMap<String, Object>) result.get("settings");
        var singleSetting = (LinkedHashMap<String, Object>)
                allSettings.get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE.name());
        var allEmployees =
                (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("employees");
        assertThat(allEmployees.contains(employee3)).isFalse(); // NOSONAR

        singleSetting = (LinkedHashMap<String, Object>)
                allSettings.get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY.name());
        var allEmployees2 =
                (ArrayList<LinkedHashMap<String, Object>>) singleSetting.get("employees");
        assertThat(allEmployees2.contains(employee3)).isFalse(); // NOSONAR
    }

    @Test
    @DisplayName("Редактирование настройки - Исключение - Неуникальный ключ \"Организация - Тип транспорта\"")
    void test_update_nonUniqueConstraintCheck() throws Exception {
        //запишем в БД еще одну настройку с новым ключом
        initializeSecondSettings();
        assertThat(settingsRepository.findAll()).hasSize(2);

        SharedRideSettings settings =
                settingsRepository.findAll().stream()
                        .filter(s -> s.getTransportType().equals(TransportTypeEnum.PERSONAL))
                        .findFirst().orElseThrow();

        SharedRideSettingsUpdateDTO updateDTO =
                SharedRideSettingsUpdateDTO.builder()
                        .id(settings.getId())
                        .settings(new HashMap<>(3))
                        .organizationId(settings.getOrganization().getId())
                        .transportType(TransportTypeEnum.TAXI.name())
                        .economyIndicationYellowRangeLowerBorder(0)
                        .economyIndicationYellowRangeUpperBorder(0)
                        .build();

        ObjectMapper objectMapper = new ObjectMapper();
        var request = objectMapper.writeValueAsString(updateDTO);

        var exception = mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(request)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResolvedException();

        assertThat(exception).isNotNull();
        assertThat(exception.getClass()).isEqualTo(DuplicateDataException.class);
    }

    @Test
    @DisplayName("Удаление настройки - успех")
    void delete_success() throws Exception {
        //дозапишем url и убедимся, что будет произведено удаление
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        url += "/" + settingsFromDb.getId();

       mockMvc.perform(delete(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        assertThat(settingsRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Удаление настройки - Исключение - Некорректный id настройки")
    void delete_incorrectSettingId() throws Exception {
        UUID newId = UUID.randomUUID();
        url += "/" + newId;

        var exception = mockMvc.perform(delete(url)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STRING)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException();

        assertThat(exception).isNotNull();
        assertThat(exception.getClass()).isEqualTo(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Создание настройки - Успех - При отсутствии первого элемента настройки")
    void create_withoutFirstSetting() throws Exception {
        //создадим новые настройки, в которой не будет первого элемента
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        Map<SharedRideSettingType, SharedRideSettingsItem> settingsItems = new HashMap<>(3);
        settingsItems.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY, new SharedRideSettingsItem());
        settingsItems.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE, new SharedRideSettingsItem());

        SharedRideSettings newSettings = SharedRideSettings.builder()
                .settings(settingsItems)
                .organization(settingsFromDb.getOrganization())
                .transportType(TransportTypeEnum.PERSONAL)
                .economyIndicationYellowRangeLowerBorder(100)
                .economyIndicationYellowRangeUpperBorder(100)
                .build();

        var result = getResponseAfterSettingsCreate(newSettings);

        assertThat(result).isNotNull()
                .containsKey("id")
                .containsEntry("organizationId", newSettings.getOrganization().getId().toString())
                .containsEntry("transportType", newSettings.getTransportType().name())
                .containsEntry("economyIndicationYellowRangeUpperBorder", newSettings.getEconomyIndicationYellowRangeUpperBorder())
        ;

        var allSettings = (LinkedHashMap<String, Object>) result.get("settings");
        assertThat(allSettings).hasSize(SharedRideSettingType.values().length);
    }

    @Test
    @DisplayName("Создание настройки - Успех - При отсутствии второго элемента настройки")
    void create_withoutSecondSetting() throws Exception {
        //создадим новые настройки, в которой не будет второго элемента
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        Map<SharedRideSettingType, SharedRideSettingsItem> settingsItems = new HashMap<>(3);
        settingsItems.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE, new SharedRideSettingsItem());
        settingsItems.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE, new SharedRideSettingsItem());

        SharedRideSettings newSettings = SharedRideSettings.builder()
                .settings(settingsItems)
                .organization(settingsFromDb.getOrganization())
                .transportType(TransportTypeEnum.PERSONAL)
                .economyIndicationYellowRangeLowerBorder(15)
                .economyIndicationYellowRangeUpperBorder(100)
                .build();

        var result = getResponseAfterSettingsCreate(newSettings);

        assertThat(result).isNotNull()
                .containsKey("id")
                .containsEntry("organizationId", newSettings.getOrganization().getId().toString())
                .containsEntry("transportType", newSettings.getTransportType().name())
                .containsEntry("economyIndicationYellowRangeUpperBorder", newSettings.getEconomyIndicationYellowRangeUpperBorder())
        ;
        var allSettings = (LinkedHashMap<String, Object>) result.get("settings");
        assertThat(allSettings).hasSize(SharedRideSettingType.values().length);
    }

    @Test
    @DisplayName("Создание настройки - Успех - При пустых списках сущностей")
    void create_withoutEntitySetIntoSettingItems() throws Exception {
        //создадим новые настройки, в которой будут пустые списки
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        Map<SharedRideSettingType, SharedRideSettingsItem> settingsItems = new HashMap<>(3);
        settingsItems.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE, new SharedRideSettingsItem());
        settingsItems.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY, new SharedRideSettingsItem());
        settingsItems.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE, new SharedRideSettingsItem());

        SharedRideSettings newSettings = SharedRideSettings.builder()
                .settings(settingsItems)
                .organization(settingsFromDb.getOrganization())
                .transportType(TransportTypeEnum.PERSONAL)
                .economyIndicationYellowRangeLowerBorder(0)
                .economyIndicationYellowRangeUpperBorder(85)
                .build();

        var result = getResponseAfterSettingsCreate(newSettings);

        assertThat(result).isNotNull()
                .containsKey("id")
                .containsEntry("organizationId", newSettings.getOrganization().getId().toString())
                .containsEntry("transportType", newSettings.getTransportType().name())
                .containsEntry("economyIndicationYellowRangeUpperBorder", newSettings.getEconomyIndicationYellowRangeUpperBorder());

        var allSettings = (LinkedHashMap<String, Object>) result.get("settings");
        assertThat(allSettings).hasSize(3);
    }

    @Test
    @DisplayName("Создание настройки - Исключение - Проверка валидатора по границам экономии (max < min)")
    void create_checkEconomyIndicationValidator() throws Exception {
        //создадим новые настройки, в которых будут перепутаны границы экономии
        SharedRideSettings settingsFromDb = settingsRepository.findAll().getFirst();
        Map<SharedRideSettingType, SharedRideSettingsItem> settingsItems = new HashMap<>(3);
        settingsItems.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE, new SharedRideSettingsItem());
        settingsItems.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY, new SharedRideSettingsItem());
        settingsItems.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE, new SharedRideSettingsItem());

        SharedRideSettings incorrect = SharedRideSettings.builder()
                .settings(settingsItems)
                .organization(settingsFromDb.getOrganization())
                .transportType(TransportTypeEnum.PERSONAL)
                .economyIndicationYellowRangeLowerBorder(80)
                .economyIndicationYellowRangeUpperBorder(50)
                .build();

        var exception = getResponseAfterTryToCreateIncorrectSettings(incorrect);

        assertThat(exception).isNotNull();
        assertThat(exception.getClass()).isEqualTo(MethodArgumentNotValidException.class);
        assertThat(exception.getMessage()).contains("First Number is less then Second");
    }
}