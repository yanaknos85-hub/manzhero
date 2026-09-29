package ru.sber.transport.corporate.business.model;

import ru.sber.transport.web.model.OrgStructureType;

import java.util.List;
import java.util.UUID;

/**
 * Фильтр сотрудников
 */
public interface EmployeeFilter extends Filter {

    /**
     * Огранизации, сотрудников которых надо отобразить
     *
     * @return список организаций
     */
    List<UUID> getOrganizations();

    /**
     * Подразделения, сотрудников которых надо отобразить
     *
     * @return список подразделений
     */
    List<UUID> getDepartments();

    /**
     * Сотрудники, которых надо отобразить
     *
     * @return список сотрудников
     */
    List<UUID> getEmployees();

    /**
     * ФИО сотрудника
     *
     * @return фамилия и инициалы сотрудника
     */
    String getFullName();

    /**
     * Табельный номер сотрудника
     *
     * @return табельный номер сотрудника
     */
    String getPersonnelNumber();

    /**
     * Человекочитаемый идентификатор сотрудника для фильтрации
     *
     * @return человекочитаемый идентификатор сотрудника
     */
    String getHumanReadableId();

    /**
     * Статус сотрудника
     *
     * @return статус сотрудника
     */
    Active getStatus();

    /**
     * Номер мобильного телефона сотрудника
     *
     * @return номер мобильного телефона сотрудника
     */
    String getMobilePhone();

    /**
     * Электронная почта сотрудника
     *
     * @return электронная почта сотрудника
     */
    String getEmail();

    /**
     * Тип оргштатной структуры
     *
     * @return тип оргштатной структуры
     */
    OrgStructureType getOrgStructureType();

}
