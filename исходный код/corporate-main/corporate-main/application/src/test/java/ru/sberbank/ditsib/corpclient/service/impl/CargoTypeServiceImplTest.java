package ru.sberbank.ditsib.corpclient.service.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.model.CargoType;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.service.CargoTypeService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Проверка сервиса типов грузов")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@Transactional
public class CargoTypeServiceImplTest {

    @Autowired
    private CargoTypeService cargoTypeService;

    @Autowired
    private OrganizationService organizationService;

    @DisplayName("Проверка сохранения типа груза")
    @Test
    void saveOtherCargoTypeTest() {
        var newOrganization = Instancio.of(Organization.class)
                .ignore(Select.field(Organization::getOrganizationGroup))
                .ignore(Select.field(Organization::getDepartments))
                .ignore(Select.field(Organization::getEmployees))
                .ignore(Select.field(Organization::getTripPurposes))
                .ignore(Select.field(Organization::getPositions))
                .ignore(Select.field(Organization::getCargoTypes))
                .ignore(Select.field(Organization::getContacts))
                .create();
        var expected = CargoType.builder()
                .name("Тестовый груз")
                .type(CargoTypeEnum.OTHER)
                .category(CargoCategoryEnum.OTHER)
                .length(12)
                .width(13)
                .height(14)
                .weight(15)
                .active(true)
                .organization(organizationService.add(newOrganization))
                .build();

        cargoTypeService.save(expected);

        var actual = cargoTypeService.getByName(expected.getName());

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getCategory(), actual.getCategory());
        assertEquals(expected.getLength(), actual.getLength());
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertEquals(expected.getWeight(), actual.getWeight());
        assertThat(expected.getVolume()).isGreaterThan(0);
        assertEquals(expected.getLength() * expected.getWidth() * expected.getHeight(), actual.getVolume());
        assertEquals(expected.isActive(), actual.isActive());
        assertEquals(expected.getOrganization(), actual.getOrganization());
    }

    @DisplayName("Проверка сохранения типа груза")
    @Test
    void saveLiquidCargoTypeTest() {
        var newOrganization = Instancio.of(Organization.class)
                .ignore(Select.field(Organization::getOrganizationGroup))
                .ignore(Select.field(Organization::getDepartments))
                .ignore(Select.field(Organization::getEmployees))
                .ignore(Select.field(Organization::getTripPurposes))
                .ignore(Select.field(Organization::getPositions))
                .ignore(Select.field(Organization::getCargoTypes))
                .ignore(Select.field(Organization::getContacts))
                .create();
        var expected = CargoType.builder()
                .name("Тестовый наливной груз")
                .type(CargoTypeEnum.OTHER)
                .category(CargoCategoryEnum.LIQUID)
                .volume(62.)
                .active(true)
                .organization(organizationService.add(newOrganization))
                .build();

        cargoTypeService.save(expected);

        var actual = cargoTypeService.getByName(expected.getName());

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getCategory(), actual.getCategory());
        assertThat(expected.getLength()).isEqualTo(0);
        assertThat(expected.getWidth()).isEqualTo(0);
        assertThat(expected.getHeight()).isEqualTo(0);
        assertThat(expected.getVolume()).isGreaterThan(0);
        assertEquals(expected.getVolume(), actual.getVolume());
        assertEquals(expected.isActive(), actual.isActive());
        assertEquals(expected.getOrganization(), actual.getOrganization());
    }
}
