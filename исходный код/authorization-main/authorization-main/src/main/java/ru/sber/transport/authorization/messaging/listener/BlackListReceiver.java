package ru.sber.transport.authorization.messaging.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.io.IOException;

/**
 * Слушатель черного списка.
 */
public interface BlackListReceiver {
    
    /**
     * Новое сообщение списка.
     *
     * @param token данные токена.
     * @throws IOException ошибка чтения токена.
     */
    @KafkaListener(topics = "${security.blacklist.jwt.topic:service.authentication.blackList}",
                   groupId = "${spring.application.name}",
                   containerFactory = "blackListKafkaListenerFactory")
    void handle(@Payload byte[] token) throws IOException;
    
}
