package ru.sber.transport.tariff_fleet.database.dao.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import ru.sber.transport.tariff_fleet.database.model.ServicePoint;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicePointTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testEquals() {
        var contractId = UUID.randomUUID();
        var servicePoint1 = new ServicePoint(UUID.randomUUID(), "Address 1", BigDecimal.valueOf(55.75), BigDecimal.valueOf(37.62), contractId, true);
        var servicePoint2 = new ServicePoint(servicePoint1.getId(), "Address 2", BigDecimal.valueOf(55.8), BigDecimal.valueOf(37.63), contractId, true);

        assertEquals(servicePoint1, servicePoint2);
    }

    @Test
    void testValidObject() {
        var validServicePoint = new ServicePoint(
                UUID.randomUUID(),
                "Москва, ул. Тверская",
                BigDecimal.valueOf(55.75),
                BigDecimal.valueOf(37.62),
                UUID.randomUUID(),
                true
        );

        var violations = validator.validate(validServicePoint);
        assertThat(violations).isEmpty();
    }

    @Test
    void testInvalidLatitudeLongitude() {
        var invalidServicePoint = new ServicePoint(
                UUID.randomUUID(),
                "",
                BigDecimal.valueOf(-100),
                BigDecimal.valueOf(200),
                UUID.randomUUID(),
                true
        );

        var violations = validator.validate(invalidServicePoint);
        assertThat(violations).hasSize(1);
    }

    @Test
    void testEmptyAddress() {
        ServicePoint emptyAddressServicePoint = new ServicePoint(
                UUID.randomUUID(),
                "",
                BigDecimal.valueOf(55.75),
                BigDecimal.valueOf(37.62),
                UUID.randomUUID(),
                true
        );

        var violations = validator.validate(emptyAddressServicePoint);
        assertThat(violations).hasSize(1);
    }

    @Test
    void testContractIdIsOptional() {
        var noContractServicePoint = new ServicePoint(
                UUID.randomUUID(),
                "Москва, Красная площадь",
                BigDecimal.valueOf(55.75),
                BigDecimal.valueOf(37.62),
                null,
                true
        );

        var violations = validator.validate(noContractServicePoint);
        assertThat(violations).isEmpty();
    }

    @Test
    void testAllArgumentsConstructor() {
        var id = UUID.randomUUID();
        var address = "Адрес";
        var lat = BigDecimal.valueOf(55.75);
        var lon = BigDecimal.valueOf(37.62);
        var contractId = UUID.randomUUID();

        var point = new ServicePoint(id, address, lat, lon, contractId, true);

        assertThat(point.getId()).isEqualTo(id);
        assertThat(point.getAddress()).isEqualTo(address);
        assertThat(point.getLatitude()).isEqualTo(lat);
        assertThat(point.getLongitude()).isEqualTo(lon);
        assertThat(point.getContractId()).isEqualTo(contractId);
    }

    @Test
    void testBuilder() {
        // Проверяем использование Builder-а
        var point = ServicePoint.builder()
                .id(UUID.randomUUID())
                .address("Москва, Лубянка")
                .latitude(BigDecimal.valueOf(55.75))
                .longitude(BigDecimal.valueOf(37.62))
                .contractId(UUID.randomUUID())
                .build();

        assertThat(point.getAddress()).isEqualTo("Москва, Лубянка");
        assertThat(point.getLatitude()).isEqualTo(BigDecimal.valueOf(55.75));
        assertThat(point.getLongitude()).isEqualTo(BigDecimal.valueOf(37.62));
    }
}