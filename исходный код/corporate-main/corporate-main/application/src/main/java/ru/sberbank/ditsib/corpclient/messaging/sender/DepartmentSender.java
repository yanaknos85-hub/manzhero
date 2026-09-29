package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.Department;

import java.util.List;

/**
 * Sending data about department.
 */
public interface DepartmentSender extends Sender<Department> {
    
    /**
     * Send department data.
     *
     * @param department department data.
     */
    default void send(Department department) {
        send(List.of(department));
    }

   
}
