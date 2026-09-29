package ru.sber.transport.tariff_fleet.database.dao.model;

import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.sber.transport.tariff_fleet.database.model.RepairTariff;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RepairTariffTest {

    @Mock
    EntityManager entityManager;

    ValidatorFactory validatorFactory;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @Test
    void testEqualsHashCode() {
        var repairTariff1 = new RepairTariff()
                .setTariffId(UUID.randomUUID())
                .setOrganizationId(UUID.randomUUID());

        var repairTariff2 = new RepairTariff()
                .setTariffId(repairTariff1.getTariffId())
                .setOrganizationId(UUID.randomUUID());

        assertEquals(repairTariff1, repairTariff2);
        assertEquals(repairTariff1.hashCode(), repairTariff2.hashCode());
    }

    @Test
    void testNotEqualByDifferentTariffIds() {
        var repairTariff1 = new RepairTariff()
                .setTariffId(UUID.randomUUID());

        var repairTariff2 = new RepairTariff()
                .setTariffId(UUID.randomUUID());

        assertNotEquals(repairTariff1, repairTariff2);
    }

    @Test
    void testValidInstance() {
        var validInstance = createValidRepairTariff();
        assertDoesNotThrow(() -> validatorFactory.getValidator().validate(validInstance));
    }

    private RepairTariff createValidRepairTariff() {
        return RepairTariff.builder()
                .tariffId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .isFieldService(true)
                .hourNormalizedPrice(1500)
                .detailDiscountPrice(BigDecimal.valueOf(10))
                .workWarranty(12)
                .mileageWarranty(50_000)
                .detailWarranty(24)
                .build();
    }
}
