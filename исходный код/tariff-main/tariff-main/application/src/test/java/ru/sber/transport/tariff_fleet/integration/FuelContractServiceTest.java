package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.dao.ContractRepository;
import ru.sber.transport.tariff_fleet.database.dao.FuelContractRepository;
import ru.sber.transport.tariff_fleet.database.dao.TariffRepository;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.service.FuelContractService;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
class FuelContractServiceTest {
    @Autowired
    private FuelContractService fuelContractService;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private FuelContractRepository fuelContractRepository;
    @Autowired
    private TariffRepository tariffRepository;

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void deactivateAndActivateFuelContract() {
        fuelContractRepository.findAll();
        var contractId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5267");
        fuelContractService.autoDeactivate(contractId);
        var tariffs = tariffRepository.findAllByContractId(contractId);
        var contract = contractRepository.findById(contractId).orElseThrow();
        assertThat(contract.isActive()).isFalse();
        assertThat(tariffs.stream().noneMatch(Tariff::isActive)).isTrue();
        fuelContractService.autoActivate(contractId);
        contract = contractRepository.findById(contractId).orElseThrow();
        tariffs = tariffRepository.findAllByContractId(contractId);
        assertThat(contract.isActive()).isTrue();
        assertThat(tariffs.stream().anyMatch(Tariff::isActive)).isTrue();
    }
}
