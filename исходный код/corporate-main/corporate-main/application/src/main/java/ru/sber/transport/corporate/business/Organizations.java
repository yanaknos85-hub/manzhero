package ru.sber.transport.corporate.business;

import ru.sber.transport.corporate.business.model.Organization;

/**
 * Бизнес-операции над организациями
 */
public interface Organizations {

    /**
     * Добавление организации
     *
     * @param source организация
     * @return новая организация
     */
    Organization add(Organization source);

}
