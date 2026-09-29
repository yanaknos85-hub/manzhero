package ru.sber.transport.fraud.monitoring.messaging.listeners.model.avro;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.Department;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;


/**
 * Данные подразделения
 */
@RequiredArgsConstructor
public final class DepartmentAvroData implements Department {

    @Delegate
    private final DepartmentMessage message;

}
