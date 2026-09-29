package ru.sber.transport.corporate.business.providers;

import ru.sber.transport.corporate.business.model.Attribute;

import java.util.List;
import java.util.UUID;

/**
 * Провайдер данных атрибутов.
 */
public interface AttributeProvider {

    /**
     * Получение атрибутов сотрудника.
     *
     * @param employeeId идентификатор сотрудника.
     * @return список атрибутов.
     */
    List<Attribute> findOfEmployee(UUID employeeId);

}
