package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.RoleActionType;

import java.util.UUID;

/**
 * Сервис для работы с историей назначения ролей.
 */
public interface RoleHistoryService {
    
    /**
     * Сохранение записи истории назначения/удаления роли.
     *
     * @param employeeId идентификатор сотрудника.
     * @param roleNames  названия ролей (через запятую).
     * @param actionType тип действия (ADD/REMOVE).
     * @param comment    комментарий к действию.
     */
    void saveHistory(UUID employeeId, String roleNames, RoleActionType actionType, String comment);

}
