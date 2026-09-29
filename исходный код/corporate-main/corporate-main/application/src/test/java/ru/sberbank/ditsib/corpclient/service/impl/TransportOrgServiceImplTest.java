package ru.sberbank.ditsib.corpclient.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;
import ru.sberbank.ditsib.corpclient.service.TransportOrgService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Проверка сервиса трнанспорта организации")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@Transactional
public class TransportOrgServiceImplTest {

    @Autowired
    private TransportOrgService transportOrgService;

    @DisplayName("Проверка сохранения траснспорта организации")
    @Test
    void saveTransportOrgTest() {
        var expected = TransportOrg.builder()
                .id(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .transportType("DOMESTIC_COURIER")
                .build();

        transportOrgService.save(expected.getOrganizationId(), expected.getTransportType());

        var actual = transportOrgService.getByOrganizationIdAndTransportType(expected.getOrganizationId(),
                expected.getTransportType()).orElse(null);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(expected.getOrganizationId(), actual.getOrganizationId());
        assertEquals(expected.getTransportType(), actual.getTransportType());
    }

    @DisplayName("Проверка удаления траснспорта организации")
    @Test
    void deleteTransportOrgTest() {
        var orgId = UUID.randomUUID();
        var transportType = "DOMESTIC_COURIER";

        transportOrgService.save(orgId, transportType);
        var saved = transportOrgService.getByOrganizationIdAndTransportType(orgId, transportType).orElse(null);

        assertNotNull(saved);
        assertNotNull(saved.getId());

        transportOrgService.delete(orgId, transportType);
        var afterDelete = transportOrgService.getByOrganizationIdAndTransportType(orgId, transportType).orElse(null);

        assertNull(afterDelete);
    }
}
