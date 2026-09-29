package ru.sber.transport.integrations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.integrations.utils.ComparatorUtils;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

@Configuration
public class CommonApiQueueConfig {

    /**
     * Queue сообщений для отправки через CommonApi
     */
    @Bean(value = "commonApiQueue")
    Queue<Message<OutContractorTaxiTripMessage>> commonApiQueue() {
        return new PriorityBlockingQueue<>(1024, ComparatorUtils.compareOutContractorTaxiTripMessage());
    }
}
