package ru.sberbank.ditsib.corpclient.database.dao;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория организаций")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class OrganizationRepositoryTest extends SharedData {

    @Autowired
    private OrganizationRepository repository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @AfterEach
    void drop() {
        repository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("Сохранение организации")
    void test_saveOrganization_correctly() {
        testOrganization1.setOrganizationCode(ORGANIZATION_CODE1);
        testOrganization1 = repository.save(testOrganization1);
        
        
        assertNotNull(testOrganization1.getId());
        Organization foundOrganization = repository.findById(testOrganization1.getId())
                                                   .orElseThrow(() -> new EntityNotFoundException("Orgaznization was" +
                                                                                                  " not saved"));
        
        assertFalse(foundOrganization.getOfficialName().isEmpty());
        assertEquals(ORGANIZATION_ADDRESS1, foundOrganization.getAddress());
        assertEquals(ORGANIZATION_OFFICIAL_NAME1, foundOrganization.getOfficialName());
        assertEquals(ORGANIZATION_CODE1, foundOrganization.getOrganizationCode());
        assertThat(foundOrganization.getDigitId()).isNotNull();
    }
    
    @Test
    @DisplayName("Поиск организации по like")
    void test_search_returnsCorrectResult() {
        repository.save(testOrganization1);
        repository.save(testOrganization2);
        
        List<Organization> byOfficialNameLike =
                repository.findByOfficialNameLikeIgnoreCase(ORGANIZATION_OFFICIAL_NAME_SHARED_PART_LIKE);
        
        assertFalse(byOfficialNameLike.isEmpty());
        assertEquals(2, byOfficialNameLike.size());
        assertEquals(ORGANIZATION_ADDRESS1, byOfficialNameLike.getFirst().getAddress());
        assertEquals(ORGANIZATION_OFFICIAL_NAME1, byOfficialNameLike.getFirst().getOfficialName());
        assertEquals(ORGANIZATION_CODE2, byOfficialNameLike.getFirst().getOrganizationCode());
    }
    
    @Test
    @DisplayName("Поиск организации по like")
    void test_search_returnsCorrectResult2() {
        repository.save(testOrganization1);
        repository.save(testOrganization2);
    
        List<Organization> byOfficialNameLike =
                repository.findByOfficialNameLikeIgnoreCase(ORGANIZATION2_OFFICIAL_NAME_PART_LIKE);
    
        assertFalse(byOfficialNameLike.isEmpty());
        assertEquals(1, byOfficialNameLike.size());
        assertEquals(ORGANIZATION_ADDRESS2, byOfficialNameLike.getFirst().getAddress());
        assertEquals(ORGANIZATION_OFFICIAL_NAME2, byOfficialNameLike.getFirst().getOfficialName());
        assertNull(byOfficialNameLike.getFirst().getOrganizationCode());
    }
    
    @Test
    @DisplayName("Тест валидации организации. Цифровой ключ не должен обновляться ")
    void test_EmployeeValidationDepartmentDigitId() {
        repository.saveAndFlush(testOrganization1);
        var org = repository.findById(testOrganization1.getId()).orElseThrow();
        var digitId = org.getDigitId();

        org.setDigitId(55L);
        repository.saveAndFlush(org);
        var actual = repository.findById(org.getId()).orElseThrow();

        assertEquals(digitId, actual.getDigitId());
    }
}
