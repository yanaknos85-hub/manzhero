package ru.sberbank.ditsib.transport.sudir.providers.role.model;

/**
 * Области видимости пользователя. По ним выдаются роли по-умолчанию.
 */
public enum Scope {

    /**
     * Пользователь - сотрудник корп. клиента.
     */
    EMPLOYEE,

    /**
     * Пользователь - водитель контрагента.
     */
    DRIVER,

    /**
     * Пользователь - диспетчер контрагента.
     */
    DISPATCHER,

    /**
     * Пользователь - контрагент.
     */
    CONTRACTOR
}
