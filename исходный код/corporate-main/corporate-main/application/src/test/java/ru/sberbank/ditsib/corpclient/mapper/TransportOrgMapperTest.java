package ru.sberbank.ditsib.corpclient.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка маппера транспорта организации")
class TransportOrgMapperTest {

    private final TransportOrgMapper mapper = new TransportOrgMapperImpl();

    @DisplayName("Проверка маппинга сущности в сообщение")
    @Test
    void toMessageTest() {
        var transportOrg = TransportOrg.builder()
                .id(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .transportType("DOMESTIC_COURIER")
                .build();
        var deleted = true;

        var actual = mapper.toMessage(transportOrg, deleted);

        assertAll(
                () -> assertEquals(transportOrg.getId(), actual.getId()),
                () -> assertEquals(transportOrg.getOrganizationId(), actual.getOrganizationId()),
                () -> assertEquals(transportOrg.getTransportType(), actual.getTransportType().name()),
                () -> assertEquals(deleted, actual.isDeleted())
        );
    }

}