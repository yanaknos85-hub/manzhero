package ru.sber.transport.corporate.messaging.senders;

import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.senders.Sender;

/**
 * Отправитель данных о сотрудниках.
 */
public interface EmployeeSender extends Sender<Employee> {
}
