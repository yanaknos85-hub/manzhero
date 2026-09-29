package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.OrganizationNameWithDepartmentInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EmbeddedPostgres
@SpringBootTest
class OrganizationRepositoryTest {
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    
    @Test
    @Sql("/scripts/organization.sql")
    void findAllByActiveTrueOrderByOfficialName() {
        var allByActiveTrueOrderByOfficialName = organizationRepository.findAllByActiveTrueOrderByOfficialName();
        assertThat(allByActiveTrueOrderByOfficialName).containsExactly(
                Organization.builder()
                            .id(UUID.fromString("46a3b980-d801-499b-87eb-be0bf3f2de53"))
                            .digitId(3L)
                            .active(true)
                            .officialName("AAA Inc")
                            .build(),
                Organization.builder()
                            .id(UUID.fromString("d41d56ab-6e33-4977-89be-5a3eaf5e72a3"))
                            .digitId(2L)
                            .active(true)
                            .officialName("BBB Inc")
                            .build());
    }
    
    @Test
    @Sql({ "/scripts/organization.sql", "/scripts/departments.sql" })
    void findByIdsWithActiveDepartments() {
        var emptyResult = organizationRepository.findByIdsWithActiveDepartments(
                List.of(UUID.fromString("c93b30b8-4a35-4c29-8d92-2fb3279a3841")));
        assertTrue(emptyResult.isEmpty());
        
        var byIdsWithActiveDepartments = organizationRepository.findByIdsWithActiveDepartments(
                List.of(UUID.fromString("46a3b980-d801-499b-87eb-be0bf3f2de53")));
        assertThat(byIdsWithActiveDepartments).containsExactlyInAnyOrder(
                new OrganizationNameWithDepartmentInfo(
                        UUID.fromString("46a3b980-d801-499b-87eb-be0bf3f2de53"),
                        "AAA Inc",
                        UUID.fromString("000098ba-5c12-423f-ba6a-de9c76db31a5"),
                        "Service Support Group",
                        null),
                new OrganizationNameWithDepartmentInfo(
                        UUID.fromString("46a3b980-d801-499b-87eb-be0bf3f2de53"),
                        "AAA Inc",
                        UUID.fromString("000091e3-48cf-4678-beba-43b6dd695e67"),
                        "Partner Relations Department",
                        UUID.fromString("000098ba-5c12-423f-ba6a-de9c76db31a5")));
    }
    
    @Test
    @Sql("/scripts/basic_corp_structure.sql")
    void findDigitIdByUserId() {
        assertThat(organizationRepository.findDigitIdByUserId(UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70"))).isEqualTo(Optional.of(8L));
        assertThat(organizationRepository.findDigitIdByUserId(UUID.fromString("46a3b980-d801-499b-87eb-be0bf3f2de53"))).isEmpty();
    }
}
