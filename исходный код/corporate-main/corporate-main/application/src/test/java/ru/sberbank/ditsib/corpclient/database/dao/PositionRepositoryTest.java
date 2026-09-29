package ru.sberbank.ditsib.corpclient.database.dao;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка репозитория должностей")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class PositionRepositoryTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private PositionRepository repository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @BeforeEach
    void iniEntities() {
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        organizationRepository.save(testOrganization1);
        organizationRepository.save(testOrganization2);
    }
    
    
    @Test
    @DisplayName("Сохранение должности")
    void test_savePosition() {
        repository.save(testPosition1);
        
        Position savedPosition = repository.findById(testPosition1.getId())
                                           .orElseThrow(() -> new EntityNotFoundException("Ошибка сохранения"));
        
        assertNotNull(savedPosition);
        assertEquals(testPosition1.getAvailableClasses(), savedPosition.getAvailableClasses());
        assertEquals(testPosition1.getName(), savedPosition.getName());
        assertEquals(testPosition1.getOrganization(), savedPosition.getOrganization());
        assertEquals(testPosition1.isSelfApproved(), savedPosition.isSelfApproved());
        
        savedPosition.getAvailableClasses().remove(TaxiClass.ECONOMY);
        repository.save(savedPosition);
        
        savedPosition = repository.findById(testPosition1.getId())
                                  .orElseThrow(() -> new EntityNotFoundException("Ошибка сохранения"));
        
        assertTrue(savedPosition.getAvailableClasses().isEmpty());
    }
    
    @Test
    @DisplayName("Сохранение должности")
    void test_removeTaxiClass() {
        repository.save(testPosition1);
        Position savedPosition = repository.findById(testPosition1.getId())
                                           .orElseThrow(() -> new EntityNotFoundException("Ошибка сохранения"));
        savedPosition.getAvailableClasses().remove(TaxiClass.ECONOMY);
        repository.save(savedPosition);
        
        savedPosition = repository.findById(testPosition1.getId())
                                  .orElseThrow(() -> new EntityNotFoundException("Ошибка сохранения"));
        
        assertTrue(savedPosition.getAvailableClasses().isEmpty());
    }
    
    @Test
    @DisplayName("Сохранение должности")
    void test_addTaxiClass() {
        repository.save(testPosition1);
        Position savedPosition = repository.findById(testPosition1.getId())
                                           .orElseThrow(() -> new EntityNotFoundException("Ошибка сохранения"));
        savedPosition.getAvailableClasses().add(TaxiClass.BUSINESS);
        repository.save(savedPosition);
        
        savedPosition = repository.findById(testPosition1.getId())
                                  .orElseThrow(() -> new EntityNotFoundException("Ошибка сохранения"));
        
        assertEquals(2, savedPosition.getAvailableClasses().size());
        assertTrue(savedPosition.getAvailableClasses().contains(TaxiClass.BUSINESS));
    }
    
    @Test
    @DisplayName("Поиск списка должностей организации")
    void test_findByOrganizationId() {
        testPosition1.setOrganization(testOrganization1);
        repository.save(testPosition1);

        testPosition2.setOrganization(testOrganization1);
        repository.save(testPosition2);
        
        List<Position> byOrganizationId = repository.findByOrganizationId(testOrganization1.getId());
        
        assertEquals(2, byOrganizationId.size());
        assertTrue(byOrganizationId.contains(testPosition1));
        assertTrue(byOrganizationId.contains(testPosition2));
    }
}