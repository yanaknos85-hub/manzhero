package ru.sberbank.ditsib.transport.approvals.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.approvals.services.GeoZoneService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка сервиса геозон")
@MockitoBean(types = JwtDecoder.class)
class GeoZoneServiceTest extends KafkaTest {
    
    @Autowired
    private GeoZoneRepository repository;
    
    private GeoZoneService geoZoneService;
    
    @BeforeEach
    void setup() {
        geoZoneService = new GeoZoneServiceImpl(repository);
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
    
        assertThat(actual.isPresent()).isTrue();
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
    
        assertThat(actual.isPresent()).isFalse();
    }
}