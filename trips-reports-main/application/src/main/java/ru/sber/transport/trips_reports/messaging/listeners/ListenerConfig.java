package ru.sber.transport.trips_reports.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sber.transport.trips_reports.messaging.mapper.MessageMapper;
import ru.sber.transport.trips_reports.providers.trips.TripProvider;
import ru.sber.transport.trips_reports.web.dto.TripStatus;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Конфигурация слушателей.
 */
@Slf4j
@Configuration
class ListenerConfig {
    @Bean
    Consumer<Message<TripReportMessage>> tripReportsInput(TripProvider tripProvider, MessageMapper messageMapper) {
        return tripReportsInputSsl(tripProvider, messageMapper);
    }

    @Bean
    Consumer<Message<TripReportMessage>> tripReportsInputSsl(TripProvider tripProvider, MessageMapper messageMapper) {
        return rawMessage -> {
            log.debug("New report message received");
            var message = rawMessage.getPayload();
            if (message.getStatus() == null) {
                logError(message.getId());
                return;
            }
            var tripStatus = TripStatus.getByRusName(message.getStatus());
            if (tripStatus != null && tripStatus.isTerminal()) {
                tripProvider.save(messageMapper.toEntity(message));
                log.debug("Report saved");
            } else logError(message.getId());
        };
    }

    private void logError(UUID tripId){
        log.error("Report message of trip %s is inconsistent, it cannot be saved".formatted(tripId));
    }

}
