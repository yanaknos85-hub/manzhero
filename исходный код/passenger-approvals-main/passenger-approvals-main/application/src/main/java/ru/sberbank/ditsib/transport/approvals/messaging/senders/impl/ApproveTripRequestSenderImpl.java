package ru.sberbank.ditsib.transport.approvals.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import ru.sber.transport.approvals.messaging.ApproveTripRequestMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveTripRequestSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of sender trip approve.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveTripRequestSenderImpl implements ApproveTripRequestSender {

    @Qualifier("tripRequestApproveOutput")
    private final ObjectProvider<OutputBridge> tripRequestApproveOutput;

    @Override
    public void sendApproved(UUID actionId, UUID approvedByEmployeeId) {
        final var message = new ApproveTripRequestMessage(actionId,
                true,
                approvedByEmployeeId,
                null,
                null
        );
        tripRequestApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, actionId)));
    }

    @Override
    public void sendDecline(UUID actionId, UUID declinedByEmployeeId, String reason) {
        final var message = new ApproveTripRequestMessage(actionId,
                false,
                declinedByEmployeeId,
                reason,
                null
        );
        log.info("Send message about trip request decline. Data: {}", message);
        tripRequestApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, actionId)));
    }

    @Override
    public void send(UUID actionId, List<UUID> approverIds) {
        final var message = new ApproveTripRequestMessage(actionId,
                null,
                null,
                null,
                approverIds
        );
        log.info("Send message about trip request need for approving. Data: {}", message);
        tripRequestApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, actionId)));
    }
}
