package ru.sberbank.ditsib.corpclient.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка static методов сервиса настройки совместных поездок")
class SharedRideSettingServiceImplTest {
    
    private Attribute attribute1;
    private Attribute attribute2;
    private Attribute attribute3;
    private Set<Attribute> attributes1;
    private Set<Attribute> attributes2;
    private Position position1;
    private Position position2;
    private Position position3;
    private Set<Position> positions1;
    private Set<Position> positions2;
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
    private Set<Employee> employees1;
    private Set<Employee> employees2;
    private SharedRideSettingsItem settingItem1;
    private SharedRideSettingsItem settingItem2;
    private Map<SharedRideSettingType, SharedRideSettingsItem> settingsMap;
    private SharedRideSettings settings;
    
    private void initialize() {
        //общий Признак - Признак3 (для проверки разрешения конфликта)
        attribute1 = Attribute.builder().name("Признак1").build();
        attribute2 = Attribute.builder().name("Признак2").build();
        attribute3 = Attribute.builder().name("Признак3").build();
        
        attributes1 = new HashSet<>();
        attributes2 = new HashSet<>();
        attributes1.add(attribute1);
        attributes1.add(attribute3);
        attributes2.add(attribute2);
        attributes2.add(attribute3);
    
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
    
        //Общая должность - Должность3 (для проверки разрешения конфликта)
        position1 = new Position();
        position1.setName("Должность1");
        position1.setId(UUID.randomUUID());
        position1.setOrganization(organization);
    
        position2 = new Position();
        position2.setName("Должность2");
        position2.setId(UUID.randomUUID());
        position2.setOrganization(organization);
    
        position3 = new Position();
        position3.setName("Должность3");
        position3.setId(UUID.randomUUID());
        position3.setOrganization(organization);
    
        positions1 = new HashSet<>();
        positions1.add(position1);
        positions1.add(position3);
    
        positions2 = new HashSet<>();
        positions2.add(position2);
        positions2.add(position3);
    
        //Общий сотрудник - Сотрудник3 (для проверки разрешения конфликта)
        employee1 = Employee.builder().id(UUID.randomUUID())
                                     .position(position1)
                                     .firstName("Имя1")
                                     .lastName("Фамилия1")
                                     .patronymic("Отчество1")
                                     .personnelNumber("Табельный№1")
                                     .attributes(attributes1)
                                     .build();
    
        employee2 = Employee.builder()
                                     .id(UUID.randomUUID())
                                     .position(position2)
                                     .firstName("Имя2")
                                     .lastName("Фамилия2")
                                     .patronymic("Отчество2")
                                     .personnelNumber("Табельный№2")
                                     .attributes(attributes2)
                                     .build();
    
        employee3 = Employee.builder()
                                     .id(UUID.randomUUID())
                                     .position(position3)
                                     .firstName("Имя3")
                                     .lastName("Фамилия3")
                                     .patronymic("Отчество3")
                                     .personnelNumber("Табельный№3")
                                     .attributes(attributes2)
                                     .build();
  
        employees1 = new HashSet<>();
        employees1.add(employee1);
        employees1.add(employee3);
    
        employees2 = new HashSet<>();
        employees2.add(employee2);
        employees2.add(employee3);
    
        settingItem1 = SharedRideSettingsItem.builder()
                                             .id(UUID.randomUUID())
                                             .positions(positions1)
                                             .attributes(attributes1)
                                             .employees(employees1)
                                             .build();
    
        settingItem2 = SharedRideSettingsItem.builder()
                                             .id(UUID.randomUUID())
                                             .positions(positions2)
                                             .attributes(attributes2)
                                             .employees(employees2)
                                             .build();
    
        settingsMap = new HashMap<>(3);
        settingsMap.put(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE, settingItem1);
        settingsMap.put(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY, settingItem2);
        settingsMap.put(SharedRideSettingType.CONFIRMATION_OF_JOIN_THE_SHARED_RIDE, settingItem2);
    
        settings = SharedRideSettings.builder()
                                     .id(UUID.randomUUID())
                                     .organization(organization)
                                     .transportType(TransportTypeEnum.TAXI)
                                     .settings(settingsMap)
                                     .build();
    }
    
    @Test
    @DisplayName("Проверка определения пересечений двух множеств типа Т")
    void test_getCommonElementsForConflictSettings() {
        Set<String> set1 = new HashSet<>();
        set1.add("Строка1");
        set1.add("Строка2");
        set1.add("Строка3");
    
        Set<String> set2 = new HashSet<>();
        set2.add("Строка1");
        set2.add("Строка3");
        set2.add("Строка4");
        set2.add("Строка5");
    
        var commonElements = SharedRideSettingServiceImpl.getCommonElementsForSets(set1, set2);
    
        assertThat(commonElements).hasSize(2)
                .contains("Строка1");
    }
    
    @Test
    @DisplayName("Проверка разрешений конфликтов")
    void test_resolveFirstTypeConflicts() {
        initialize();
        final int attributes1size = attributes1.size();
        final int attributes2size = attributes2.size();
        final int positions1size = positions1.size();
        final int positions2size = positions2.size();
        final int employees1size = employees1.size();
        final int employees2size = employees2.size();
        /*
        в результате разрешения конфликтов из списков настроек INDIVIDUAL_RIDE_IS_NOT_AVAILABLE и
        CAN_RIDE_INDIVIDUALLY_ONLY должны пропасть attribute3, employee3, position3
        */
        SharedRideSettingServiceImpl.resolveConflicts(settings);
    
        assertThat(settingItem1.getEmployees().size()).isNotEqualTo(employees1size);
        assertThat(settingItem1.getAttributes().size()).isNotEqualTo(attributes1size);
        assertThat(settingItem1.getPositions().size()).isNotEqualTo(positions1size);
        assertThat(settingItem2.getEmployees().size()).isNotEqualTo(employees2size);
        assertThat(settingItem2.getAttributes().size()).isNotEqualTo(attributes2size);
        assertThat(settingItem2.getPositions().size()).isNotEqualTo(positions2size);
    }
}