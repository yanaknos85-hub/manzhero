package ru.sberbank.ditsib.corpclient.mapper;

import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.dto.mapper.SettingsItemDTOMapper;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка маппера для настроек совместных поездок")
class SettingsDTOMapperTest {
    
    private final SettingsItemDTOMapper mapper = Mappers.getMapper(SettingsItemDTOMapper.class);
    
    private Attribute attribute1;
    private Attribute attribute2;
    private Attribute attribute3;
    private Set<Attribute> attributes1;
    private Set<Attribute> attributes2;
    private Set<Attribute> allAttributes;
    private Organization organization;
    private Position position1;
    private Position position2;
    private Set<Position> positions;
    private Employee employee1;
    private Employee employee2;
    private Set<Employee> employees;
    private SharedRideSettingsItem settingItem;
    private Map<SharedRideSettingType, SharedRideSettingsItem> settingsMap;
    private SharedRideSettings settings;
    
    private void initializeEntities() {
        attribute1 = Attribute.builder().name("Признак1").build();
        attribute2 = Attribute.builder().name("Признак2").build();
        attribute3 = Attribute.builder().name("Признак3").build();
    
        attributes1 = new HashSet<>();
        attributes2 = new HashSet<>();
        allAttributes = new HashSet<>();
        attributes1.add(attribute1);
        attributes1.add(attribute3);
        attributes2.add(attribute2);
        attributes2.add(attribute3);
        allAttributes.add(attribute1);
        allAttributes.add(attribute2);
        allAttributes.add(attribute3);
    
        organization = new Organization();
        organization.setId(UUID.randomUUID());
    
        position1 = new Position();
        position1.setName("Должность1");
        position1.setId(UUID.randomUUID());
        position1.setOrganization(organization);
    
        position2 = new Position();
        position2.setName("Должность2");
        position2.setId(UUID.randomUUID());
        position2.setOrganization(organization);
    
        positions = new HashSet<>();
        positions.add(position1);
        positions.add(position2);
    
        employee1 = Employee.builder()
                            .id(UUID.randomUUID())
                            .position(position1)
                            .firstName("Имя1")
                            .lastName("Фамилия1")
                            .patronymic("Отчество1")
                            .personnelNumber("Табельный№1")
                            .attributes(attributes1)
                            .build();
    
        employee2 = Employee.builder()
                            .id(UUID.randomUUID())
                            .position(position1)
                            .firstName("Имя2")
                            .lastName("Фамилия2")
                            .patronymic("Отчество2")
                            .personnelNumber("Табельный№2")
                            .attributes(attributes2)
                            .build();
    
        employees = new HashSet<>();
        employees.add(employee1);
        employees.add(employee2);
    
        settingItem = SharedRideSettingsItem.builder()
                                            .id(UUID.randomUUID())
                                            .positions(positions)
                                            .attributes(allAttributes)
                                            .employees(employees)
                                            .build();
    
        settingsMap = new HashMap<>(3);
        settingsMap.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE, settingItem);
        settingsMap.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE, settingItem);
        settingsMap.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY, settingItem);
    
        settings = SharedRideSettings.builder()
                                     .id(UUID.randomUUID())
                                     .organization(organization)
                                     .transportType(TransportTypeEnum.TAXI)
                                     .settings(settingsMap)
                                     .economyIndicationYellowRangeLowerBorder(27)
                                     .economyIndicationYellowRangeUpperBorder(63)
                                     .build();
    }
    
    @Test
    @DisplayName("Проверка маппинга Признак сотрудника -> DTO")
    void test_attributeToDTO() {
        Attribute attribute = Attribute.builder().name("Признак1").id(UUID.randomUUID()).build();
        var attributeDTO = mapper.attributeToDto(attribute);
        
        Assertions.assertThat(attributeDTO.getName()).isEqualTo(attribute.getName());
        Assertions.assertThat(attributeDTO.getId()).isEqualTo(attribute.getId());
    }
    
    @Test
    @DisplayName("Проверка маппинга DTO -> Признак сотрудника")
    void test_dtoToAttribute() {
        SettingsAttributeDTO dto = SettingsAttributeDTO.builder().name("Признак1").id(UUID.randomUUID()).build();
        var attribute = mapper.dtoToAttribute(dto);
        
        Assertions.assertThat(attribute.getName()).isEqualTo(dto.getName());
        Assertions.assertThat(attribute.getId()).isEqualTo(dto.getId());
    }
    
    @Test
    @DisplayName("Проверка маппинга Должность -> DTO")
    void test_positionToDto() {
        Position position = new Position();
        position.setId(UUID.randomUUID());
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        position.setOrganization(organization);
        position.setName("Должность1");
        SettingsPositionDTO positionDTO = mapper.positionToDto(position);
        
        Assertions.assertThat(positionDTO.getId()).isEqualTo(position.getId());
        Assertions.assertThat(positionDTO.getName()).isEqualTo(position.getName());
    }
    
    @Test
    @DisplayName("Проверка маппинга DTO -> Должность")
    void test_dtoToPosition() {
        SettingsPositionDTO dto = SettingsPositionDTO.builder()
                                                     .id(UUID.randomUUID())
                                                     .name("Должность1")
                                                     .build();
        Position position = mapper.dtoToPosition(dto);
    
        Assertions.assertThat(position.getId()).isEqualTo(dto.getId());
        Assertions.assertThat(position.getName()).isEqualTo(dto.getName());
    }
    
    @Test
    @DisplayName("Проверка маппинга Сотрудник -> DTO")
    void test_employeeToDto() {
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        Position position = new Position();
        position.setId(UUID.randomUUID());
        position.setOrganization(organization);
        
        Employee employee = Employee.builder().id(UUID.randomUUID())
                                    .position(position)
                                    .firstName("Имя")
                                    .lastName("Фамилия")
                                    .patronymic("Отчество")
                                    .personnelNumber("Табельный№")
                                    .build();
        var dto = mapper.employeeToDto(employee);
    
        Assertions.assertThat(dto.getId()).isEqualTo(employee.getId());
        Assertions.assertThat(dto.getFirstName()).isEqualTo(employee.getFirstName());
        Assertions.assertThat(dto.getPersonnelNumber()).isEqualTo(employee.getPersonnelNumber());
        Assertions.assertThat(dto.getPositionId()).isEqualTo(employee.getPosition().getId());
    }
    
    @Test
    @DisplayName("Проверка маппинга DTO -> Сотрудник")
    void test_dtoToEmployee() {
        SettingsEmployeeDTO dto = SettingsEmployeeDTO.builder()
                                                     .id(UUID.randomUUID())
                                                     .positionId(UUID.randomUUID())
                                                     .firstName("Имя")
                                                     .lastName("Фамилия")
                                                     .patronymic("Отчество")
                                                     .personnelNumber("Табельный№")
                                                     .build();
        var employee = mapper.dtoToEmployee(dto);
    
        Assertions.assertThat(employee.getId()).isEqualTo(dto.getId());
        Assertions.assertThat(employee.getFirstName()).isEqualTo(dto.getFirstName());
        Assertions.assertThat(employee.getPersonnelNumber()).isEqualTo(dto.getPersonnelNumber());
        Assertions.assertThat(employee.getPosition().getId()).isEqualTo(dto.getPositionId());
    }
    
    @Test
    @DisplayName("Проверка маппинга Настройки -> DTO")
    void test_sharedRideSettingToDto() {
        initializeEntities();
    
        var itemCreateDto = mapper.sharedRideSettingsItemToCreateDto(settingItem);
        var createDto = mapper.sharedRideSettingsToCreateDto(settings);
        var itemUpdateDto = mapper.sharedRideSettingsItemToUpdateDto(settingItem);
        var updateDto = mapper.sharedRideSettingsToUpdateDto(settings);
       
        assertThat(itemCreateDto.getEmployees()).hasSize(2);
        Assertions.assertThat(itemCreateDto.getEmployees().stream().filter(e -> e.getId().equals(employee1.getId()))
                                           .findFirst().get().getId()).isEqualTo(employee1.getId());
        assertThat(itemCreateDto.getPositions()).hasSize(2);
        Assertions.assertThat(itemCreateDto.getPositions().stream().filter(p -> p.getId().equals(position2.getId()))
                                           .findFirst().get().getId()).isEqualTo(position2.getId());
        assertThat(itemCreateDto.getAttributes()).hasSize(3);
        Assertions
                .assertThat(itemCreateDto.getAttributes().stream().filter(a -> a.getName().equals(attribute3.getName()))
                                         .findFirst().get().getName()).isEqualTo(attribute3.getName());
        Assertions.assertThat(createDto.getTransportType()).isEqualTo(settings.getTransportType().name());
        Assertions.assertThat(createDto.getOrganizationId()).isEqualTo(settings.getOrganization().getId());
        assertThat(createDto.getSettings()).hasSize(3);
        assertThat(createDto.getSettings().get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE.name())
                            .getAttributes()).hasSize(3);
        assertThat(createDto.getEconomyIndicationYellowRangeLowerBorder()).isEqualTo(settings.getEconomyIndicationYellowRangeLowerBorder());
        assertThat(createDto.getEconomyIndicationYellowRangeUpperBorder()).isEqualTo(settings.getEconomyIndicationYellowRangeUpperBorder());
        
        assertThat(itemUpdateDto.getEmployees()).hasSize(2);
        Assertions.assertThat(itemUpdateDto.getEmployees().stream().filter(e -> e.getId().equals(employee1.getId()))
                                           .findFirst().get().getId()).isEqualTo(employee1.getId());
        assertThat(itemUpdateDto.getPositions()).hasSize(2);
        Assertions.assertThat(itemUpdateDto.getPositions().stream().filter(p -> p.getId().equals(position2.getId()))
                                           .findFirst().get().getId()).isEqualTo(position2.getId());
        assertThat(itemUpdateDto.getAttributes()).hasSize(3);
        Assertions
                .assertThat(itemUpdateDto.getAttributes().stream().filter(a -> a.getName().equals(attribute3.getName()))
                                         .findFirst().get().getName()).isEqualTo(attribute3.getName());
        Assertions.assertThat(updateDto.getId()).isEqualTo(settings.getId());
        Assertions.assertThat(updateDto.getTransportType()).isEqualTo(settings.getTransportType().name());
        Assertions.assertThat(updateDto.getOrganizationId()).isEqualTo(settings.getOrganization().getId());
        assertThat(updateDto.getSettings().get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY.name())
                            .getAttributes()).hasSize(3);
        assertThat(updateDto.getEconomyIndicationYellowRangeLowerBorder()).isEqualTo(settings.getEconomyIndicationYellowRangeLowerBorder());
        assertThat(updateDto.getEconomyIndicationYellowRangeUpperBorder()).isEqualTo(settings.getEconomyIndicationYellowRangeUpperBorder());
    }
    
    @SuppressWarnings({"OptionalGetWithoutIsPresent", "java:S5961"})
    @Test
    @DisplayName("Проверка маппинга DTO -> Настройки")
    void test_dtoToSharedRideSetting() {
        SettingsAttributeDTO attributeDto1 = SettingsAttributeDTO.builder().name("Признак1").build();
        SettingsAttributeDTO attributeDto2 = SettingsAttributeDTO.builder().name("Признак2").build();
        SettingsAttributeDTO attributeDto3 = SettingsAttributeDTO.builder().name("Признак3").build();
    
        Set<SettingsAttributeDTO> attributesDto1 = new HashSet<>();
        Set<SettingsAttributeDTO> attributesDto2 = new HashSet<>();
        Set<SettingsAttributeDTO> allAttributesDto = new HashSet<>();
        attributesDto1.add(attributeDto1);
        attributesDto1.add(attributeDto3);
        attributesDto2.add(attributeDto2);
        attributesDto2.add(attributeDto3);
        allAttributesDto.add(attributeDto1);
        allAttributesDto.add(attributeDto2);
        allAttributesDto.add(attributeDto3);
        
        UUID organizationId = UUID.randomUUID();
        
        SettingsPositionDTO positionDto1 = SettingsPositionDTO.builder()
                                                              .id(UUID.randomUUID())
                                                              .name("Должность1")
                                                              .build();
        
        SettingsPositionDTO positionDto2 = SettingsPositionDTO.builder()
                                                              .id(UUID.randomUUID())
                                                              .name("Должность2")
                                                              .build();
        
        Set<SettingsPositionDTO> positionsDto = new HashSet<>();
        positionsDto.add(positionDto1);
        positionsDto.add(positionDto2);
    
        SettingsEmployeeDTO employeeDto1 = SettingsEmployeeDTO.builder()
                                                              .id(UUID.randomUUID())
                                                              .positionId(positionDto1.getId())
                                                              .firstName("Имя1")
                                                              .lastName("Фамилия1")
                                                              .patronymic("Отчество1")
                                                              .personnelNumber("Табельный№1")
                                                              .build();
        
        SettingsEmployeeDTO employeeDto2 = SettingsEmployeeDTO.builder()
                                                              .id(UUID.randomUUID())
                                                              .positionId(positionDto2.getId())
                                                              .firstName("Имя2")
                                                              .lastName("Фамилия2")
                                                              .patronymic("Отчество2")
                                                              .personnelNumber("Табельный№2")
                                                              .build();
                                     
        Set<SettingsEmployeeDTO> employeesDto = new HashSet<>();
        employeesDto.add(employeeDto1);
        employeesDto.add(employeeDto2);
    
        SharedRideSettingsItemCreateDTO itemCreateDTO =
                SharedRideSettingsItemCreateDTO.parentBuilder()
                                               .attributes(allAttributesDto)
                                               .employees(employeesDto)
                                               .positions(positionsDto)
                                               .build();
    
        Map<String, SharedRideSettingsItemCreateDTO> createDtoMap = new HashMap<>(3);
        createDtoMap.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE.name(), itemCreateDTO);
        createDtoMap.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE.name(), itemCreateDTO);
        createDtoMap.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY.name(), itemCreateDTO);
    
        SharedRideSettingsCreateDTO createDTO =
                SharedRideSettingsCreateDTO.builder()
                                           .organizationId(organizationId)
                                           .transportType(TransportTypeEnum.CARSHARING.name())
                                           .settings(createDtoMap)
                                           .economyIndicationYellowRangeLowerBorder(0)
                                           .economyIndicationYellowRangeUpperBorder(0)
                                           .build();
        
        var createSettingsItem = mapper.createDtoToSharedRideSettingsItem(itemCreateDTO);
        var createSettings = mapper.createDtoToSharedRideSettings(createDTO);
        
        assertThat(createSettingsItem.getEmployees()).hasSize(2);
        Assertions.assertThat(createSettingsItem.getEmployees().stream().filter(e -> e.getId().equals(employeeDto1.getId()))
                                                .findFirst().get().getId()).isEqualTo(employeeDto1.getId());
        assertThat(createSettingsItem.getPositions()).hasSize(2);
        Assertions.assertThat(createSettingsItem.getPositions().stream().filter(p -> p.getId().equals(positionDto2.getId()))
                                                .findFirst().get().getId()).isEqualTo(positionDto2.getId());
        assertThat(createSettingsItem.getAttributes()).hasSize(3);
        Assertions.assertThat(createSettingsItem.getAttributes().stream().filter(a -> a.getName().equals(attributeDto3.getName()))
                                                .findFirst().get().getName()).isEqualTo(attributeDto3.getName());
        
        Assertions.assertThat(createSettings.getTransportType().name()).isEqualTo(createDTO.getTransportType());
        Assertions.assertThat(createSettings.getOrganization().getId()).isEqualTo(createDTO.getOrganizationId());
        assertThat(createSettings.getId()).isNull();
        assertThat(createSettings.getSettings()).hasSize(3);
        assertThat(createSettings.getSettings().get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE)
                                 .getEmployees()).hasSize(2);
        assertThat(createSettings.getEconomyIndicationYellowRangeLowerBorder()).isEqualTo(createDTO.getEconomyIndicationYellowRangeLowerBorder());
        assertThat(createSettings.getEconomyIndicationYellowRangeUpperBorder()).isEqualTo(createDTO.getEconomyIndicationYellowRangeUpperBorder());
    
        SharedRideSettingsItemUpdateDTO itemUpdateDTO =
                SharedRideSettingsItemUpdateDTO.childBuilder()
                                               .id(UUID.randomUUID())
                                               .attributes(allAttributesDto)
                                               .employees(employeesDto)
                                               .positions(positionsDto)
                                               .build();
    
        Map<String, SharedRideSettingsItemUpdateDTO> updateDtoMap = new HashMap<>(3);
        updateDtoMap.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE.name(), itemUpdateDTO);
        updateDtoMap.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE.name(), itemUpdateDTO);
        updateDtoMap.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY.name(), itemUpdateDTO);
    
        SharedRideSettingsUpdateDTO updateDTO =
                SharedRideSettingsUpdateDTO.builder()
                                           .id(UUID.randomUUID())
                                           .organizationId(organizationId)
                                           .transportType(TransportTypeEnum.CARSHARING.name())
                                           .settings(updateDtoMap)
                                           .economyIndicationYellowRangeLowerBorder(100)
                                           .economyIndicationYellowRangeUpperBorder(100)
                                           .build();
        
        //создать сущность настроек, которую будем менять. Создадим новые данные для нее
        initializeEntities();
        //убедимся в отличии данных dto и settings. Id разный (только в тесте) и не должен быть перезаписан
        Assertions.assertThat(settings.getId()).isNotEqualTo(updateDTO.getId());
        Assertions.assertThat(settings.getTransportType().name()).isNotEqualTo(updateDTO.getTransportType());
    
        var updateSettingsItem = mapper.updateDtoToSharedRideSettingsItem(itemUpdateDTO);
        mapper.updateDtoToSharedRideSettings(updateDTO, settings);
        
        Assertions.assertThat(updateSettingsItem.getId()).isEqualTo(itemUpdateDTO.getId());
        assertThat(updateSettingsItem.getEmployees()).hasSize(2);
        Assertions.assertThat(updateSettingsItem.getEmployees().stream().filter(e -> e.getId().equals(employeeDto1.getId()))
                                                .findFirst().get().getId()).isEqualTo(employeeDto1.getId());
        assertThat(updateSettingsItem.getPositions()).hasSize(2);
        Assertions.assertThat(updateSettingsItem.getPositions().stream().filter(p -> p.getId().equals(positionDto2.getId()))
                                                .findFirst().get().getId()).isEqualTo(positionDto2.getId());
        assertThat(updateSettingsItem.getAttributes()).hasSize(3);
        Assertions.assertThat(updateSettingsItem.getAttributes().stream().filter(a -> a.getName().equals(attributeDto3.getName()))
                                                .findFirst().get().getName()).isEqualTo(attributeDto3.getName());
        //убедимся, что id не перезаписан
        Assertions.assertThat(settings.getId()).isNotEqualTo(updateDTO.getId());
        //убедимся, что transportType перезаписан
        Assertions.assertThat(settings.getTransportType().name()).isEqualTo(updateDTO.getTransportType());
        Assertions.assertThat(settings.getOrganization().getId()).isEqualTo(updateDTO.getOrganizationId());
        assertThat(settings.getSettings()).hasSize(3);
        assertThat(settings.getSettings().get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY)
                                 .getEmployees()).hasSize(2);
        assertThat(Objects.equals(settings.getEconomyIndicationYellowRangeLowerBorder(),
                                  updateDTO.getEconomyIndicationYellowRangeLowerBorder())).isTrue();
        assertThat(Objects.equals(settings.getEconomyIndicationYellowRangeUpperBorder(),
                                  updateDTO.getEconomyIndicationYellowRangeUpperBorder())).isTrue();
    }
}