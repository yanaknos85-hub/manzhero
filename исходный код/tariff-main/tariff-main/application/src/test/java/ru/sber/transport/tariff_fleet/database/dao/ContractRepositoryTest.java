package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Contract;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql(scripts = { "/scripts/db_cleanup.sql",
                 "/scripts/basic_corp_structure.sql",
                 "/scripts/edf_operator.sql",
                 "/scripts/ewb_contract.sql",
                 "/scripts/repair_contract.sql",
                 "/scripts/fuel_contract.sql",
})
class ContractRepositoryTest {
    
    @Autowired
    private ContractRepository contractRepository;
    
    @Test
    void getTypeById() {
        var actualNull = contractRepository.getTypeById(UUID.fromString("1ec7ad78-04a5-4e39-ab89-eae5f7b5f184"));
        assertThat(actualNull).isEmpty();
        
        var actualEwb = contractRepository.getTypeById(UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435"));
        assertThat(actualEwb).contains(DocumentType.EWB);
        
        var actualRepair = contractRepository.getTypeById(UUID.fromString("999d5145-73e0-4518-aba2-bf9b3ea81088"));
        assertThat(actualRepair).contains(DocumentType.REPAIR_AND_MAINTENANCE);

        var actualFuel = contractRepository.getTypeById(UUID.fromString("780916d0-b750-4e54-abe3-c253885fffc1"));
        assertThat(actualFuel).contains(DocumentType.FUEL);
    }
    
    @Test
    void findStartedContracts() {
        var startedContracts = contractRepository.findStartedContracts();
        assertThat(startedContracts.stream().map(Contract::getId).toList())
                .containsExactlyInAnyOrder(UUID.fromString("80cea1a8-b869-49d0-b0dd-de0079fbb134"));
    }
    
    @Test
    void findEndedContracts() {
        var endedContracts = contractRepository.findEndedContracts();
        assertThat(endedContracts.stream().map(Contract::getId).toList())
                .containsExactlyInAnyOrder(UUID.fromString("be8ba3fc-5064-4ad9-9972-7aa33079516d"));
    }
    
}
