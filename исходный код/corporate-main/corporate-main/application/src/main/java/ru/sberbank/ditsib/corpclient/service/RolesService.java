package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.messages.Role;

import java.util.Optional;

/**
 * Сервис работы с ролями.
 */
public interface RolesService {
    
    /**
     * Удаление.
     *
     * @param code код роли для удаления.
     */
    void delete(String code);
    
    /**
     * Получение роли.
     *
     * @param code код роли.
     * @return роль.
     */
    Optional<Role> get(String code);
    
    /**
     * Получение роли по имени.
     *
     * @param name имя роли.
     * @return роль.
     */
    Optional<Role> getByName(String name);
    
    /**
     * Сохранение роли.
     *
     * @param role роль для сохранения.
     */
    void save(Role role);
}
