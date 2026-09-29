package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql({ "/scripts/db_cleanup.sql",
       "/scripts/basic_corp_structure.sql",
       "/scripts/edf_operator.sql",
       "/scripts/fleet_owner_organization.sql" })
class FleetOwnerOrganizationRepositoryTest {
    
    @Autowired
    private FleetOwnerOrganizationRepository fleetOwnerOrganizationRepository;
    
    @Test
    void findAllActive() {
        var expected1 = new GetAllActiveOrganizationNamesDto(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"), "ЦА");
        var expected2 = new GetAllActiveOrganizationNamesDto(UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"), "Тест2");
        assertThat(fleetOwnerOrganizationRepository.findAllActive())
                .usingRecursiveComparison()
                .isEqualTo(List.of(expected2, expected1));
    }
}