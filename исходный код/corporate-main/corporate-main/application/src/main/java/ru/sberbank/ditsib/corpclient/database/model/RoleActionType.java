package ru.sberbank.ditsib.corpclient.database.model;

/**
 * Типы действий с ролями.
 */
public enum RoleActionType {

    /**
     * Назначение роли 'Курьер' сотруднику.
     */
    ADD_COURIER,

    /**
     * Отзыв роли 'Курьер' у сотрудника.
     */
    REMOVE_COURIER,

    /**
     * Обновление списка ролей сотрудника (изменение множества ролей).
     */
    UPDATE,

    /**
     * Деактивация всех ролей у сотрудника.
     */
    REMOVE_ALL
}
