package ru.sber.transport.fraud.monitoring.messaging.listeners.model.avro;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.Position;
import ru.sber.transport.messages.corporate.avro.PositionMessage;


/**
 * Данные о должности
 */
@RequiredArgsConstructor
public class PositionAvroData implements Position {

    @Delegate
    private final PositionMessage delegatee;

}
