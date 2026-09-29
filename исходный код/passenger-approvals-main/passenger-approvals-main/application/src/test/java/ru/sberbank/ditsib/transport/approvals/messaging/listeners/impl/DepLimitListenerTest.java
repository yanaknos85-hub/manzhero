package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.database.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.DepLimit;
import ru.sberbank.ditsib.transport.approvals.messaging.message.LimitMessage;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@DisplayName("Проверка получения лимитов департамента")
@MockitoBean(types = {JwtDecoder.class})
public class DepLimitListenerTest extends KafkaTest {

    @Autowired
    private DepLimitRepository repository;

    @Autowired
    @Qualifier("limitInput")
    private Consumer<Message<LimitMessage>> limitInput;
    
    @AfterEach
    void afterEach() {
        repository.deleteAll();
    }
    
    @Test
    @DisplayName("Новое")
    void handleDepLimit_new() {
        assertThat(repository.count()).isEqualTo(0);
        var message = baseLimitMessage().build();
        limitInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getOwnerId()).isEqualTo(message.getOwnerId());
        assertThat(actual.getYear()).isEqualTo(message.getYear());
        assertThat(actual.getSum()).isEqualTo(message.getSum().intValue());
        assertThat(actual.getReserve()).isEqualTo(message.getReserve().intValue());
        assertThat(actual.getDeleted()).isFalse();
        
    }
    
    @Test
    @DisplayName("Редактирование")
    void handleDelegate_edit() {
        assertThat(repository.count()).isZero();
        var limit = DepLimit.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .parentId(UUID.randomUUID())
                .year(LocalDate.now().getYear())
                .sum(100L)
                .reserve(200L).build();
        repository.save(limit);
        assertThat(repository.count()).isEqualTo(1);
        
        var message = baseLimitMessage().id(limit.getId()).build();
        limitInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getOwnerId()).isEqualTo(message.getOwnerId());
        assertThat(actual.getYear()).isEqualTo(message.getYear());
        assertThat(actual.getSum()).isEqualTo(message.getSum().intValue());
        assertThat(actual.getReserve()).isEqualTo(message.getReserve().intValue());
        assertThat(actual.getDeleted()).isFalse();
        
    }
    
    @Test
    @DisplayName("Удаление")
    void handleDelegate_delete() {
        assertThat(repository.count()).isEqualTo(0);
        var limit = DepLimit.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .parentId(UUID.randomUUID())
                .year(LocalDate.now().getYear())
                .sum(100L)
                .reserve(200L).build();
        repository.save(limit);
        assertThat(repository.count()).isEqualTo(1);
        
        var message = baseLimitMessage().id(limit.getId()).deleted(true).build();
        limitInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getDeleted()).isTrue();
    }
    
    @Test
    @DisplayName("Удаление не существующего лимита")
    void handleDepLimit_remove_notExists() {
        assertThat(repository.count()).isEqualTo(0);
        var message = baseLimitMessage().deleted(true).build();
        limitInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Новое. Сообщение с null reserve")
    void handleDepLimit_ReserveNull() {
        assertThat(repository.count()).isEqualTo(0);
        var message = baseLimitMessage().reserve(null).build();
        limitInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getOwnerId()).isEqualTo(message.getOwnerId());
        assertThat(actual.getYear()).isEqualTo(message.getYear());
        assertThat(actual.getSum()).isEqualTo(message.getSum().intValue());
        assertThat(actual.getReserve()).isEqualTo(0);
        assertThat(actual.getDeleted()).isFalse();
        
    }
    
    private LimitMessage.LimitMessageBuilder baseLimitMessage() {
        return LimitMessage.builder()
                           .id(UUID.randomUUID())
                           .limitType(DepLimitListenerImpl.DEPARTMENT_TYPE)
                           .departmentId(UUID.randomUUID())
                           .ownerId(UUID.randomUUID())
                           .year(LocalDate.now().getYear())
                           .sum(10L)
                           .reserve(20L);
    }
    
}