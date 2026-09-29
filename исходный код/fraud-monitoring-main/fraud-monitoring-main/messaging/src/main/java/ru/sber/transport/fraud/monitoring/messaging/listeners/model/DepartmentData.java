package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.Department;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.UUID;


/**
 * Данные подразделения
 */
@RequiredArgsConstructor
public final class DepartmentData implements Department {

    @Delegate
    private final DepartmentMessage message;

    @Override
    public UUID getHeadId() {
        return message.getDepartmentHeadId();
    }

    @Override
    public String getName() {
        return message.getDepartmentName();
    }
}
