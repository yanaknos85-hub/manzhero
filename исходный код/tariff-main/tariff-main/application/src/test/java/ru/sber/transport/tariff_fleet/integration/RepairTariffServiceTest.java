package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.dao.ContractRepository;
import ru.sber.transport.tariff_fleet.database.dao.RepairContractRepository;
import ru.sber.transport.tariff_fleet.database.dao.TariffRepository;
import ru.sber.transport.tariff_fleet.dto.repair.RepairTariffDto;
import ru.sber.transport.tariff_fleet.service.tariff.impl.RepairTariffServiceImpl;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
@EmbeddedPostgres
class RepairTariffServiceTest {

    @Autowired
    private RepairTariffServiceImpl repairTariffServiceImpl;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private RepairContractRepository repairContractRepository;
    @Autowired
    private TariffRepository tariffRepository;

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void testDeactivateTariff() {
        var id = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5268");
        repairTariffServiceImpl.deactivateTariff(id);
        assertThat(tariffRepository.findById(id).orElseThrow().isActive()).isFalse();
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void testGetTariff() {
        var id = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5268");
        var tariff = repairTariffServiceImpl.getTariff(id);
        assertInstanceOf(RepairTariffDto.class, tariff);
    }
}
