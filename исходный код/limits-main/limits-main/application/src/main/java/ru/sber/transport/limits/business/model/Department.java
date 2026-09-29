package ru.sber.transport.limits.business.model;

import lombok.Data;

import java.util.UUID;

/**
 * Модель подразделения
 */
@Data
public class Department {

    /**
     * Человекочитаемый идентификатор подразделения
     */
    private String humanReadableId;

    /**
     * Идентификатор организации, которой принадлежит подразделение
     */
    private UUID organizationId;

    /**
     * Код подразделения
     */
    private String code;

    /**
     * Название подразделения
     */
    private String name;

    /**
     * Руководитель подразделением
     */
    private Employee departmentHead;

    /**
     * Идентификатор подразделения
     */
    private UUID id;

}
