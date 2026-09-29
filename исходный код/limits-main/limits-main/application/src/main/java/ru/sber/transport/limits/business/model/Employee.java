package ru.sber.transport.limits.business.model;

import lombok.Data;

import java.util.UUID;

/**
 * Сотрудник
 */
@Data
public class Employee {

    /**
     * Идентификатор сотрудника
     */
    private UUID id;

    /**
     * Идентификатор организации
     */
    private UUID organizationId;

    /**
     * Адрес электронной почты сотрудника
     */
    private String email;

    /**
     * Имя сотрудника
     */
    private String firstName;

    /**
     * Фамилия сотрудника
     */
    private String lastName;

    /**
     * Отчество сотрудника
     */
    private String patronymic;

    /**
     * Табельный номер сотрудника
     */
    private String personnelNumber;

    /**
     * Человекочитаемый идентификатор сотрудника
     */
    private String humanReadableId;

    /**
     * Идентификатор должности сотрудника
     */
    private UUID positionId;

    /**
     * Флаг активности сотрудника
     */
    private boolean active;

    /**
     * Идентификатор подразделения сотрудника
     */
    private UUID departmentId;

    /**
     * Идентификатор руководителя сотрудника
     */
    private UUID supervisorId;

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

}
