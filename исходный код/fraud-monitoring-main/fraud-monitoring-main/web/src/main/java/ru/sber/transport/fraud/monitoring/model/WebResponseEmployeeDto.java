package ru.sber.transport.fraud.monitoring.model;

import ru.sber.transport.web.model.EmployeeDto;

/**
 * Объект ответа с данными о сотруднике.
 */
public class WebResponseEmployeeDto extends EmployeeDto {

    /**
     * Конструктор для создания ответа с данными о сотруднике.
     *
     * @param employee данные о сотруднике
     */
    public WebResponseEmployeeDto(ru.sber.transport.fraud.monitoring.model.Employee employee) {
        setId(employee.getId());
        setLastName(employee.getLastName());
        setFirstName(employee.getFirstName());
        setPatronymic(employee.getPatronymic());
        setPersonnelNumber(employee.getPersonnelNumber());
    }
}
