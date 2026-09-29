package ru.sberbank.ditsib.transport.approvals.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.approvals.database.model.Approver;
import ru.sberbank.ditsib.transport.approvals.mappers.DepartmentTripRequestApproversMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.DepartmentTripRequestApproversSender;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of sender trip approve.
 */
@Service
@RequiredArgsConstructor
public class DepartmentTripRequestApproversSenderImpl implements DepartmentTripRequestApproversSender {

    @Qualifier("departmentTripRequestApproversOutput")
    private final ObjectProvider<OutputBridge> departmentTripRequestApproversOutput;
    private final DepartmentTripRequestApproversMapper mapper;
    
    @Override
    public void send(UUID departmentId, Collection<Approver> approvers) {
        final var message = mapper.toMessage(departmentId, approvers);
        departmentTripRequestApproversOutput.ifAvailable(it -> it.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }
}
