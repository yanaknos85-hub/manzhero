package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.mapper.ExecutorGroupMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.ExecutorGroupSender;

/**
 * Implementation of executor group sender.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ExecutorGroupSenderImpl implements ExecutorGroupSender {

    @Qualifier("executorGroupOutput")
    private final ObjectProvider<OutputBridge> executorGroupOutput;

    @Qualifier("executorGroupOutputSsl")
    private final ObjectProvider<OutputBridge> executorGroupOutputSsl;

    private final ExecutorGroupMapper mapper;

    @Override
    public void send(ExecutorGroupDTO executorGroup) {
        var message = mapper.toMessage(executorGroup);
        executorGroupOutput.ifAvailable(ob -> ob.send(message));
        executorGroupOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
