package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.Position;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;


/**
 * Данные о должности
 */
@RequiredArgsConstructor
public class PositionData implements Position {

    @Delegate
    private final PositionMessage delegatee;

    @Override
    public String getName() {
        return delegatee.getPositionName();
    }
}
