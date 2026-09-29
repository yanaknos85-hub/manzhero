package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.model.ContractorFilter;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql(scripts = { "/scripts/contractor.sql" })
class ContractorRepositoryTest {
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Test
    void findAllByActiveTrueOrderByNameAsc() {
        var actualList = contractorRepository.findAllByServiceTypeAndActiveTrueOrderByNameAsc(ServiceType.AUTOSERVICE);
        assertThat(actualList).containsExactly(
                Contractor.builder()
                          .id(UUID.fromString("a033de41-2228-40ba-bc13-e97849625484"))
                          .name("ContractorName2")
                          .contractorType(ContractorType.API)
                          .serviceType(ServiceType.AUTOSERVICE)
                          .build(),
                Contractor.builder()
                          .id(UUID.fromString("5c79574d-3c79-4dec-86e4-2e0432b53266"))
                          .name("ContractorName4")
                          .contractorType(ContractorType.AUTOSERVICE_INTERNAL)
                          .serviceType(ServiceType.AUTOSERVICE)
                          .build());
    }
    
    @Test
    @Sql({ "/scripts/db_cleanup.sql",
           "/scripts/basic_corp_structure.sql",
           "/scripts/contractors_find_by_document_type.sql"
    })
    void findContractorsByFilter() {
        assertThat(contractorRepository.findContractorsByFilter(ContractorFilter.builder()
                             .active(true)
                             .serviceType(ServiceType.AUTOSERVICE)
                             .documentType(DocumentType.REPAIR_AND_MAINTENANCE)
                             .selfOnly(true)
                             .organizationId(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                             .build())).hasSize(1);
    }
}
