package ru.sberbank.ditsib.transport.sudir.messaging.providers;

import ru.sber.transport.roles.messages.RoleMessage;

/**
 * Поставщик даннх ролей.
 */
public interface RoleProvider {
    
    /**
     * Удаление роли.
     *
     * @param code код роли для удаления.
     */
    void delete(String code);
    
    /**
     * Сохранение роли.
     *
     * @param message данные роли для сохранения.
     */
    void save(RoleMessage message);
    
}
