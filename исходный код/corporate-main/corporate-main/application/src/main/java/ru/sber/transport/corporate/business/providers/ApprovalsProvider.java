package ru.sber.transport.corporate.business.providers;

import java.util.UUID;

/**
 * Предоставление данных о согласованиях.
 */
public interface ApprovalsProvider {

    /**
     * Количество согласований сотрудника.
     *
     * @param employeeId идентификатор сотрудника.
     * @return количество согласований.
     */
    Integer countOfEmployee(UUID employeeId);

}
