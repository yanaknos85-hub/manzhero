package ru.sberbank.ditsib.transport.approvals.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import ru.sber.transport.approvals.messaging.ApproveFinalTripMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveFinalTripSender;

import java.util.Map;
import java.util.UUID;

/**
 * Implementation of sender trip approve.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveFinalTripSenderImpl implements ApproveFinalTripSender {

    @Qualifier("finalTripApproveOutput")
    private final ObjectProvider<OutputBridge> finalTripApproveOutput;

    @Override
    public void sendApproved(UUID requestId, UUID approvedByEmployeeId) {
        final var message = new ApproveFinalTripMessage(
                requestId,
                true,
                approvedByEmployeeId,
                null
        );
        log.info("Send message about final trip approve. Data: {}", message);
        finalTripApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }

    @Override
    public void sendDecline(UUID requestId, UUID declinedByEmployeeId, String reason) {
        final var message = new ApproveFinalTripMessage(
                requestId,
                false,
                declinedByEmployeeId,
                reason
        );
        log.info("Send message about final trip decline. Data: {}", message);
        finalTripApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }

}
