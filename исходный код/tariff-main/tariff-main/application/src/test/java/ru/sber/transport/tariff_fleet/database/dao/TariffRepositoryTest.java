package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.constant.DocumentType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql(scripts = { "/scripts/basic_corp_structure.sql",
                 "/scripts/edf_operator.sql",
                 "/scripts/ewb_contract.sql",
                 "/scripts/fleet_owner_organization.sql",
                 "/scripts/ewb_tariff.sql" })
class TariffRepositoryTest {
    
    @Autowired
    private TariffRepository tariffRepository;
    
    @Test
    void getTypeById() {
        var actualNull = tariffRepository.getTypeById(UUID.fromString("4d8109e7-4081-4617-8e28-fe6c31c049bb"));
        assertThat(actualNull).isEmpty();
        
        var actualEwb = tariffRepository.getTypeById(UUID.fromString("3c2d9e7a-cc85-7b05-8692-263cb759408d"));
        assertThat(actualEwb).contains(DocumentType.EWB);
    }
}
