package ru.sber.transport.fraud.monitoring.messaging.listeners.model.avro;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.Employee;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;


/**
 * Данные сотрудника
 */
@RequiredArgsConstructor
public final class EmployeeAvroData implements Employee {

    @Delegate
    private final EmployeeMessage delegate;

}
