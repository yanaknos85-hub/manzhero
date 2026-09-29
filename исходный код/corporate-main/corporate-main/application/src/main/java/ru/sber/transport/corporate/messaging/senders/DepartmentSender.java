package ru.sber.transport.corporate.messaging.senders;

import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.senders.Sender;

/**
 * Отправитель данных о подразделениях.
 */
public interface DepartmentSender extends Sender<Department> {

    /**
     * Отправить данные.
     *
     * @param department подразделение.
     */
    void send(Department department);

}
