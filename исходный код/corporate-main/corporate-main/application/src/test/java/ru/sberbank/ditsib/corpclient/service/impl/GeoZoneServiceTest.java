package ru.sberbank.ditsib.corpclient.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.service.GeoZoneService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@MockitoBean(types = {JwtDecoder.class, OutputBridge.class})
@Transactional
@DisplayName("Проверка сервиса геозон")
@ActiveProfiles("test")
class GeoZoneServiceTest {
    
    @Autowired
    private GeoZoneRepository repository;
    
    private GeoZoneService geoZoneService;
    
    @BeforeEach
    void setup() {
        geoZoneService = new GeoZoneServiceImpl(repository);
    }
    
    @Test
    @DisplayName("Добавление")
    void test_add() {
        assertThat(repository.count()).isZero();
        
        var geoZone = new GeoZone();
        
        geoZone.setId(UUID.randomUUID());
        geoZone.setName("Name");
        geoZone.setCode(100 + "");
        geoZone.setParentId(UUID.randomUUID());
        
        geoZoneService.save(geoZone);
    
        assertThat(repository.count()).isEqualTo(1);
        
        var actualDb = repository.findAll().getFirst();
        
        assertThat(actualDb.getId()).isEqualTo(geoZone.getId());
        assertThat(actualDb.getName()).isEqualTo(geoZone.getName());
        assertThat(actualDb.getCode()).isEqualTo(geoZone.getCode());
        assertThat(actualDb.getParentId()).isEqualTo(geoZone.getParentId());
    }
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var id = UUID.randomUUID();
        
        var geoZone = new GeoZone();
    
        geoZone.setId(id);
        geoZone.setName("Name");
        geoZone.setCode(100 + "");
        geoZone.setParentId(UUID.randomUUID());
        
        repository.save(geoZone);
        
        var actual = geoZoneService.get(id);
    
        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(geoZone.getId());
        assertThat(actual.get().getName()).isEqualTo(geoZone.getName());
        assertThat(actual.get().getCode()).isEqualTo(geoZone.getCode());
        assertThat(actual.get().getParentId()).isEqualTo(geoZone.getParentId());
    }
    
    @Test
    @DisplayName("Получение несуществующего")
    void test_get_unexists() {
        var id = UUID.randomUUID();
        
        var actual = geoZoneService.get(id);
    
        assertThat(actual).isNotPresent();
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var id = UUID.randomUUID();
        
        var geoZone = new GeoZone();
        
        geoZone.setId(id);
        geoZone.setName("Name");
        geoZone.setCode(100 + "");
        geoZone.setParentId(UUID.randomUUID());
        
        repository.save(geoZone);
        
        assertThat(repository.count()).isEqualTo(1);
        
        geoZoneService.delete(geoZone);
    
        assertThat(repository.count()).isZero();
    }
    
}