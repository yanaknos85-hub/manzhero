package ru.sberbank.ditsib.corpclient.messaging.initializations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.initialization.TopicInitialization;
import ru.sberbank.ditsib.corpclient.database.dao.DelegateRepository;
import ru.sberbank.ditsib.corpclient.database.model.RecordStatus;
import ru.sberbank.ditsib.corpclient.messaging.sender.DelegateSender;

import org.springframework.transaction.annotation.Transactional;

/**
 * Инициализация топика с данными о делегатах. Топик будет проинициализирован только если его нет или он пуст.
 */
@RequiredArgsConstructor
@Component
class DelegateTopicInitialization implements TopicInitialization {
    
    private final DelegateRepository delegateRepository;
    
    private final DelegateSender delegateSender;

    @Qualifier("delegateOutput")
    private final ObjectProvider<OutputBridge> delegateOutput;

    @Qualifier("delegateOutputSsl")
    private final ObjectProvider<OutputBridge> delegateOutputSsl;

    @Qualifier("delegateOutputAvro")
    private final ObjectProvider<OutputBridge> delegateOutputAvro;

    @Override
    public OutputBridge channel() {
        return delegateOutput.getIfAvailable(() -> delegateOutputSsl.getIfAvailable(delegateOutputAvro::getIfAvailable));
    }
    
    @Transactional
    @Override
    public void initialize() {
        var delegates = delegateRepository.findAll();
        delegates.forEach(delegate -> {
            if (RecordStatus.ACTIVE.equals(delegate.getStatus())) {
                delegateSender.send(delegate);
            } else {
                delegateSender.sendDelete(delegate);
            }
        });
    }
}
