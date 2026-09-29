package ru.sberbank.ditsib.transport.approvals.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import ru.sber.transport.approvals.messaging.ApproveSharedRideMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.approvals.database.model.SharedRideJoinApproval;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApproveSharedRideSender;

import java.util.Map;

/**
 * Implementation of sender shared ride approve.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveSharedRideSenderImpl implements ApproveSharedRideSender {

    @Qualifier("sharedRideApproveOutput")
    private final ObjectProvider<OutputBridge> sharedRideApproveOutput;

    @Override
    public void sendApproved(SharedRideJoinApproval approval) {
        final var message = new ApproveSharedRideMessage(
                approval.getAddRequestId(),
                approval.getActionId(),
                true,
                approval.getApprovedById(),
                approval.getDesiredDate(),
                approval.getStatus().name(),
                null
        );
        log.info("Send message about shared ride approve. Data: {}", message);
        sharedRideApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }

    @Override
    public void sendDecline(SharedRideJoinApproval approval, String reason) {
        final var message = new ApproveSharedRideMessage(
                approval.getAddRequestId(),
                approval.getActionId(),
                false,
                approval.getApprovedById(),
                approval.getDesiredDate(),
                approval.getStatus().name(),
                reason
        );
        log.info("Send message about shared ride decline. Data: {}", message);
        sharedRideApproveOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }
}
