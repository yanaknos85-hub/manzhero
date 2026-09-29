package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.Employee;

import java.util.List;

/**
 * Отправитель данных о сотруднике.
 */
public interface EmployeeSender extends Sender<Employee> {
    
    /**
     * Отправить данные.
     *
     * @param employee данные сотрудника.
     */
    default void send(Employee employee) {
        send(List.of(employee));
    }

}