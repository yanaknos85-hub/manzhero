package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
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
import ru.sberbank.ditsib.transport.approvals.database.dao.RequestDocumentRepository;
import ru.sberbank.ditsib.transport.approvals.database.dao.ViewDocumentRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.RequestDocument;
import ru.sberbank.ditsib.transport.approvals.database.model.ViewDocument;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestDocumentMessage;
import ru.sberbank.ditsib.transport.approvals.messaging.message.ViewDocumentMessage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EmbeddedPostgres
@DisplayName("Проверка слушателей документов заявок")
@MockitoBean(types = {JwtDecoder.class})
@Disabled("Требуется переработка")
public class RequestDocumentListenerTest extends KafkaTest {
    
    @Autowired
    @Qualifier("requestDocumentInput")
    private Consumer<Message<RequestDocumentMessage>> requestDocumentInput;
    
    @Autowired
    @Qualifier("viewDocumentInput")
    private Consumer<Message<ViewDocumentMessage>> viewDocumentInput;
    
    @Autowired
    private RequestDocumentRepository repository;
    
    @Autowired
    private ViewDocumentRepository viewRepository;
    
    @AfterEach
    void afterEach() {
        viewRepository.deleteAll();
        repository.deleteAll();
    }
    
    @Test
    @DisplayName("Новый документ")
    public void testCreateDocument() {
        assertThat(repository.count()).isEqualTo(0);
        
        RequestDocumentMessage message = createDocument();
        
        RequestDocument actual = repository.findAll().get(0);
        assertThat(actual.getDocumentId()).isEqualTo(message.getDocumentId());
        assertThat(actual.getEmployeeId()).isEqualTo(message.getEmployeeId());
        assertThat(actual.getRequestId()).isEqualTo(message.getRequestId());
        assertThat(actual.getViews().size()).isEqualTo(0);
        
        assertThat(repository.findById(message.getDocumentId())).isNotNull();
    }
    
    @Test
    @DisplayName("Удаление")
    public void testRemoveDocument() {
        assertThat(repository.count()).isEqualTo(0);
        
        RequestDocumentMessage message = createDocument();
        
        
        RequestDocumentMessage removeMessage = RequestDocumentMessage.builder()
                                                                     .documentId(message.getDocumentId())
                                                                     .employeeId(UUID.randomUUID())
                                                                     .requestId(message.getRequestId())
                                                                     .deleted(true)
                                                                     .build();
        requestDocumentInput.accept(MessageBuilder.withPayload(removeMessage).build());
        assertThat(repository.count()).isEqualTo(0);
        
/*
        // Test send already removed
        assertThatThrownBy(() -> produceMessage(documentSink, removeMessage))
                .hasCauseInstanceOf(EmptyResultDataAccessException.class)
        ;
*/
    
    }
    
    @Test
    @DisplayName("Просмотр")
    public void testViewDocument() {
        assertThat(repository.count()).isEqualTo(0);
        
        RequestDocumentMessage message = createDocument();
        
        
        ViewDocumentMessage viewMessage = ViewDocumentMessage.builder()
                                                             .documentId(message.getDocumentId())
                                                             .employeeId(UUID.randomUUID())
                                                             .dateTime(LocalDateTime.now())
                                                             .build();
        viewDocumentInput.accept(MessageBuilder.withPayload(viewMessage).build());
        assertThat(repository.count()).isEqualTo(1);
        RequestDocument actual = repository.findAll().get(0);
        assertThat(actual.getDocumentId()).isEqualTo(message.getDocumentId());
        assertThat(actual.getEmployeeId()).isEqualTo(message.getEmployeeId());
        assertThat(actual.getRequestId()).isEqualTo(message.getRequestId());
        assertThat(actual.getViews().size()).isEqualTo(1);
        ViewDocument view = actual.getViews().iterator().next();
        assertThat(view.getEmployeeId()).isEqualTo(viewMessage.getEmployeeId());
        assertThat(view.getDateTime()).isCloseTo(viewMessage.getDateTime(), within(1, ChronoUnit.SECONDS));
        
        // Второй просмотр
        viewMessage = ViewDocumentMessage.builder()
                                         .documentId(message.getDocumentId())
                                         .employeeId(UUID.randomUUID())
                                         .dateTime(LocalDateTime.now().plusHours(1))
                                         .build();
        viewDocumentInput.accept(MessageBuilder.withPayload(viewMessage).build());
        assertThat(repository.count()).isEqualTo(1);
        actual = repository.findAll().get(0);
        assertThat(actual.getViews().size()).isEqualTo(2);
        view = actual.getViews().stream().sorted(Comparator.comparing(ViewDocument::getDateTime))
                     .toList()
                     .get(1);
        assertThat(view.getEmployeeId()).isEqualTo(viewMessage.getEmployeeId());
        assertThat(view.getDateTime()).isCloseTo(viewMessage.getDateTime(), within(1, ChronoUnit.SECONDS));
        
    }
    
    /**
     * Сначала приходит просмотр, а потом сам документ
     */
    @Test
    @DisplayName("Сначала приходит просмотр, а потом сам документ")
    public void testViewBeforeDocument() {
        assertThat(repository.count()).isEqualTo(0);
        assertThat(viewRepository.count()).isEqualTo(0);
        
        ViewDocumentMessage viewMessage = ViewDocumentMessage.builder()
                                                             .documentId(UUID.randomUUID())
                                                             .employeeId(UUID.randomUUID())
                                                             .dateTime(LocalDateTime.now())
                                                             .build();
        viewDocumentInput.accept(MessageBuilder.withPayload(viewMessage).build());
        assertThat(repository.count()).isEqualTo(0);
        assertThat(viewRepository.count()).isEqualTo(1);
    
        ViewDocument view = viewRepository.findAll().iterator().next();
        assertThat(view.getEmployeeId()).isEqualTo(viewMessage.getEmployeeId());
        assertThat(view.getDocumentId()).isEqualTo(viewMessage.getDocumentId());
        assertThat(view.getDateTime()).isCloseTo(viewMessage.getDateTime(), within(1, ChronoUnit.SECONDS));
    
    
        RequestDocumentMessage message = RequestDocumentMessage.builder()
                                                               .documentId(viewMessage.getDocumentId())
                                                               .requestId(UUID.randomUUID())
                                                               .employeeId(UUID.randomUUID())
                                                               .build();
        requestDocumentInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);
        assertThat(viewRepository.count()).isEqualTo(1);

        RequestDocument actual = repository.findAll().get(0);
        assertThat(actual.getDocumentId()).isEqualTo(message.getDocumentId());
        assertThat(actual.getEmployeeId()).isEqualTo(message.getEmployeeId());
        assertThat(actual.getRequestId()).isEqualTo(message.getRequestId());
        assertThat(actual.getViews().size()).isEqualTo(1);
        view = actual.getViews().iterator().next();
        assertThat(view.getEmployeeId()).isEqualTo(viewMessage.getEmployeeId());
        assertThat(view.getDateTime()).isCloseTo(viewMessage.getDateTime(), within(1, ChronoUnit.SECONDS));
        
        // Второй просмотр
        viewMessage = ViewDocumentMessage.builder()
                                         .documentId(message.getDocumentId())
                                         .employeeId(UUID.randomUUID())
                                         .dateTime(LocalDateTime.now().plusHours(1))
                                         .build();
        viewDocumentInput.accept(MessageBuilder.withPayload(viewMessage).build());
        assertThat(repository.count()).isEqualTo(1);
        actual = repository.findAll().get(0);
        assertThat(actual.getViews().size()).isEqualTo(2);
        view = actual.getViews().stream().sorted(Comparator.comparing(ViewDocument::getDateTime))
                     .toList()
                     .get(1);
        assertThat(view.getEmployeeId()).isEqualTo(viewMessage.getEmployeeId());
        assertThat(view.getDateTime()).isCloseTo(viewMessage.getDateTime(), within(1, ChronoUnit.SECONDS));
        
    }
    
    @Test
    @DisplayName("Удаление с просмотрами")
    public void testRemoveDocumentWithViews() {
        assertThat(repository.count()).isEqualTo(0);
        
        RequestDocumentMessage message = createDocument();
    
        ViewDocumentMessage viewMessage = ViewDocumentMessage.builder()
                                                             .documentId(message.getDocumentId())
                                                             .employeeId(UUID.randomUUID())
                                                             .dateTime(LocalDateTime.now())
                                                             .build();
        viewDocumentInput.accept(MessageBuilder.withPayload(viewMessage).build());
    
        assertThat(repository.count()).isEqualTo(1);
        assertThat(viewRepository.count()).isEqualTo(1);
        
        RequestDocumentMessage removeMessage = RequestDocumentMessage.builder()
                                                                     .documentId(message.getDocumentId())
                                                                     .employeeId(UUID.randomUUID())
                                                                     .requestId(message.getRequestId())
                                                                     .deleted(true)
                                                                     .build();
        requestDocumentInput.accept(MessageBuilder.withPayload(removeMessage).build());
        assertThat(repository.count()).isEqualTo(0);
        assertThat(viewRepository.count()).isEqualTo(0);
        
        
    }
    
    private RequestDocumentMessage createDocument() {
        RequestDocumentMessage message = RequestDocumentMessage.builder()
                                                               .documentId(UUID.randomUUID())
                                                               .employeeId(UUID.randomUUID())
                                                               .requestId(UUID.randomUUID())
                                                               .build();
        requestDocumentInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);
        return message;
    }
    
    
}
