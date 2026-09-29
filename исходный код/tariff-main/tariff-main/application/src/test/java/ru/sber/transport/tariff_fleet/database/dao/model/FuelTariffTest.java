package ru.sber.transport.tariff_fleet.database.dao.model;

import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.sber.transport.tariff_fleet.database.model.FuelTariff;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FuelTariffTest {

    @Mock
    EntityManager entityManager;

    ValidatorFactory validatorFactory;

    @BeforeEach
    void setUp() {
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @Test
    void testEqualsHashCode() {
        var fuelTariff1 = new FuelTariff()
                .setTariffId(UUID.randomUUID())
                .setOrganizationId(UUID.randomUUID());

        var fuelTariff2 = new FuelTariff()
                .setTariffId(fuelTariff1.getTariffId()) // тот же ID
                .setOrganizationId(UUID.randomUUID()); // другой orgID

        assertEquals(fuelTariff1, fuelTariff2);
        assertEquals(fuelTariff1.hashCode(), fuelTariff2.hashCode());
    }

    @Test
    void testNotEqualByDifferentTariffIds() {
        var fuelTariff1 = new FuelTariff()
                .setTariffId(UUID.randomUUID());

        var fuelTariff2 = new FuelTariff()
                .setTariffId(UUID.randomUUID()); // другой ID

        assertNotEquals(fuelTariff1, fuelTariff2);
    }

    @Test
    void testValidInstance() {
        var validInstance = createValidFuelTariff();
        assertDoesNotThrow(() -> validatorFactory.getValidator().validate(validInstance));
    }


    private FuelTariff createValidFuelTariff() {
        return FuelTariff.builder()
                .tariffId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .discount(BigDecimal.valueOf(5))
                .build();
    }
}
