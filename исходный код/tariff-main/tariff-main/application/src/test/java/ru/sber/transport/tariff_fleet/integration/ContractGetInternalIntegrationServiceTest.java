package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.service.ContractGetInternalService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedPostgres
class ContractGetInternalIntegrationServiceTest {

    @Autowired
    private ContractGetInternalService contractGetInternalService;


    @Test
    @SneakyThrows
    @Sql({ "/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contractor.sql",
            "/scripts/repair_contract.sql" })
    void getContractTypeTest() {
        assertEquals(DocumentType.REPAIR_AND_MAINTENANCE, contractGetInternalService.getContractType(UUID.fromString("999d5145-73e0-4518-aba2-bf9b3ea81088")));
    }

    @Test
    @SneakyThrows
    @Sql({ "/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contractor.sql",
            "/scripts/repair_contract.sql" })
    void getContractTest() {
        assertTrue(contractGetInternalService.getContract(UUID.fromString("999d5145-73e0-4518-aba2-bf9b3ea81088")).isPresent());
    }
}
