package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TripPurposeMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EmbeddedPostgres
@Transactional
@DisplayName("Проверка получения целей")
@MockitoBean(types = {JwtDecoder.class})
class TripPurposeListenerTest extends KafkaTest {
    
    @Autowired
    private TripPurposeRepository repository;
    
    @Autowired
    @Qualifier("tripPurposeInput")
    private Consumer<Message<TripPurposeMessage>> tripPurposeInput;
    
    @Test
    @DisplayName("Новая цель")
    void handle_new() {
        var id = UUID.randomUUID();
        var message = TripPurposeMessage.builder()
                                        .id(id)
                                        .label("В киоск")
                                        .build();
        
        assertThat(repository.count()).isZero();
        assertEquals(0, repository.count());

        tripPurposeInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isEqualTo(1);
        var position = repository.findAll().get(0);
        assertThat(position.getId()).isEqualTo(message.getId());
        assertThat(position.getLabel()).isEqualTo(message.getLabel());
    }
    
}
