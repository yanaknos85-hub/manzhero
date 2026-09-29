package ru.sber.transport.tariff_fleet.constant;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionTypeTest {

    @Test
    void getMedicineTypes() {
        var result = InspectionType.getMedicineTypes();
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(EnumSet.of(InspectionType.MEDIC, InspectionType.TELEMEDIC));
    }
}
