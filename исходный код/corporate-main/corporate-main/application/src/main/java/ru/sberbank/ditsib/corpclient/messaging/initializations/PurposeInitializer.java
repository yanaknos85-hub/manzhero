package ru.sberbank.ditsib.corpclient.messaging.initializations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.corpclient.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.corpclient.messaging.sender.PurposeSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.initialization.TopicInitialization;

import org.springframework.transaction.annotation.Transactional;

/**
 * Инициализатор топика организации.
 */
@RequiredArgsConstructor
@Component
class PurposeInitializer implements TopicInitialization {
    
    private final TripPurposeRepository repository;
    
    private final PurposeSender sender;

    @Qualifier("purposeOutput")
    private final ObjectProvider<OutputBridge> purposeOutput;

    @Qualifier("purposeOutputSsl")
    private final ObjectProvider<OutputBridge> purposeOutputSsl;

    @Qualifier("purposeOutputAvro")
    private final ObjectProvider<OutputBridge> purposeOutputAvro;

    @Override
    public OutputBridge channel() {
        return purposeOutput.getIfAvailable(() -> purposeOutputSsl.getIfAvailable(purposeOutputAvro::getIfAvailable));
    }
    
    @Transactional
    @Override
    public void initialize() {
        repository.findAll().forEach(sender::send);
    }
}
