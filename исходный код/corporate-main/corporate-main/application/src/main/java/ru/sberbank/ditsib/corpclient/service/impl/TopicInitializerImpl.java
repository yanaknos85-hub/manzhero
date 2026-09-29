package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.service.TopicInitializer;
import ru.sber.transport.messaging.kafka.initialization.TopicInitialization;

import java.util.List;

/**
 * Реализация инициализации.
 */
@RequiredArgsConstructor
@Component
class TopicInitializerImpl implements TopicInitializer {
    
    private final List<TopicInitialization> initializators;
    
    @Async
    @Override
    public void initialize(String key) {
        if ("Волшебное_слово".equals(key)) {
            initializators.forEach(TopicInitialization::initialize);
        }
    }
}
