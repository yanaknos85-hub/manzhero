package ru.sber.transport.authorization.messaging.listener.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.messaging.listener.BlackListReceiver;
import ru.sber.transport.authorization.service.BlackListService;

import java.io.IOException;
import java.util.Map;

/**
 * Реализация получателя черного списка.
 */
@RequiredArgsConstructor
@Component
@ConditionalOnBean(name = "blackListKafkaListenerFactory")
public class BlackListReceiverImpl implements BlackListReceiver {
    
    private final BlackListService service;
    
    private final ObjectMapper mapper;
    
    @Override
    public void handle(@Payload byte[] token) throws IOException {
        var tokenData = mapper.readValue(token, new TypeReference<Map<String, String>>() {});
        service.add(tokenData.getOrDefault("token", null));
    }
}
