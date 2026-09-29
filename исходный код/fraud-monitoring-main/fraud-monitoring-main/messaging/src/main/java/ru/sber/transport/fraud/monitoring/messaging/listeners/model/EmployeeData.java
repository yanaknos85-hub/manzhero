package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.fraud.monitoring.model.Employee;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;


/**
 * Данные сотрудника
 */
@RequiredArgsConstructor
public final class EmployeeData implements Employee {

    @Delegate
    private final EmployeeMessage delegate;

}
