package ru.sberbank.ditsib.corpclient.messaging.listener.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.service.GeoZoneService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.GeoZoneMessage;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка получателя геозон")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles("test")
class GeoZoneListenerImplTest {

    @MockitoBean
    private GeoZoneService geoZoneService;

    @Autowired
    private Consumer<Message<GeoZoneMessage>> geoZoneInput;
    
    @Test
    @DisplayName("Сообщение с новыми данными")
    void test_message_new() {
        var id = UUID.randomUUID();
        
        var message = new GeoZoneMessage();
        message.setCode(100 + "");
        message.setId(id);
        message.setName("Name");
        message.setParentId(UUID.randomUUID());
        
        when(geoZoneService.get(id)).thenReturn(Optional.empty());

        geoZoneInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
        
        var geoZoneCaptor = ArgumentCaptor.forClass(GeoZone.class);
        
        verify(geoZoneService).save(geoZoneCaptor.capture());
        
        var actual = geoZoneCaptor.getValue();
        
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getCode()).isEqualTo(message.getCode());
        assertThat(actual.getName()).isEqualTo(message.getName());
        assertThat(actual.getParentId()).isEqualTo(message.getParentId());
    }
    
    @Test
    @DisplayName("Сообщение с измененными данными")
    void test_message_edited() {
        var id = UUID.randomUUID();
        
        var message = new GeoZoneMessage();
        message.setCode(1000 + "");
        message.setId(id);
        message.setName("Name");
        message.setParentId(UUID.randomUUID());
        
        var geoZone = new GeoZone();
        geoZone.setId(id);
        message.setCode(10000 + "");
        message.setName("Name2");
        geoZone.setParentId(UUID.randomUUID());
        
        when(geoZoneService.get(id)).thenReturn(Optional.of(geoZone));

        geoZoneInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
    
        var geoZoneCaptor = ArgumentCaptor.forClass(GeoZone.class);
    
        verify(geoZoneService).save(geoZoneCaptor.capture());
    
        var actual = geoZoneCaptor.getValue();
    
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getCode()).isEqualTo(message.getCode());
        assertThat(actual.getName()).isEqualTo(message.getName());
        assertThat(actual.getParentId()).isEqualTo(message.getParentId());
    }
    
    @Test
    @DisplayName("Сообщение с удаленными данными")
    void test_message_deleted() {
        var id = UUID.randomUUID();
    
        var message = new GeoZoneMessage();
        message.setCode(100 + "");
        message.setId(id);
        message.setName("Name");
        message.setParentId(UUID.randomUUID());
        message.setDeleted(true);
    
        var geoZone = new GeoZone();
        geoZone.setId(id);
        message.setCode(10000 + "");
        message.setName("Name2");
        geoZone.setParentId(UUID.randomUUID());
    
        when(geoZoneService.get(id)).thenReturn(Optional.of(geoZone));

        geoZoneInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
    
        var geoZoneCaptor = ArgumentCaptor.forClass(GeoZone.class);
    
        verify(geoZoneService).delete(geoZoneCaptor.capture());
    
        var actual = geoZoneCaptor.getValue();
    
        assertThat(actual.getId()).isEqualTo(geoZone.getId());
        assertThat(actual.getCode()).isEqualTo(geoZone.getCode());
        assertThat(actual.getName()).isEqualTo(geoZone.getName());
        assertThat(actual.getParentId()).isEqualTo(geoZone.getParentId());
    }
}