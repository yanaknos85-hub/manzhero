package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.service.SubContractInternalService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EmbeddedPostgres
class SubContractInternalServiceIntegrationTest {
    @Autowired
    private SubContractInternalService<RepairContract> repairContractSubContractInternalService;
    @Autowired
    private SubContractInternalService<FuelContract> fuelContractSubContractInternalService;


    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void repairContractGet() {
        var contract = repairContractSubContractInternalService.getContract(UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5268"));
        assertNotNull(contract);
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void fuelContractGet() {
        var contract = fuelContractSubContractInternalService.getContract(UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5267"));
        assertNotNull(contract);
    }

}
