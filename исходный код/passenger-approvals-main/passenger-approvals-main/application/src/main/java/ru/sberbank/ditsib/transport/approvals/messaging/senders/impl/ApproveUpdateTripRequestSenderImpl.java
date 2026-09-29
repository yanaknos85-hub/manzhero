package ru.sberbank.ditsib.transport.approvals.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import ru.sber.transport.approvals.messaging.ApproveUpdateTripRequestMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveUpdateTripRequestSender;

import java.util.Map;
import java.util.UUID;

/**
 * Implementation of sender trip approve.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveUpdateTripRequestSenderImpl implements ApproveUpdateTripRequestSender {

    @Qualifier("updateTripApproveOutput")
    private final ObjectProvider<OutputBridge> updateTripApproveOutput;

    @Override
    public void sendApproved(UUID updateId, UUID approvedByEmployeeId) {
        final var message = new ApproveUpdateTripRequestMessage(
                updateId,
                true,
                approvedByEmployeeId,
                null
        );
        log.info("Send message about update trip approve. Data: {}", message);
        updateTripApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }

    @Override
    public void sendDecline(UUID updateId, UUID declinedByEmployeeId, String reason) {
        final var message = new ApproveUpdateTripRequestMessage(
                updateId,
                false,
                declinedByEmployeeId,
                reason
        );
        log.info("Send message about final trip decline. Data: {}", message);
        updateTripApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }
}
