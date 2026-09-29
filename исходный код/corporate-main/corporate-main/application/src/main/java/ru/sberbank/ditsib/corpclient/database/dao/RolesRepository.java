package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.corpclient.database.model.messages.Role;

import java.util.Optional;

/**
 * Репозиторий для работы с ролями.
 */
public interface RolesRepository extends JpaRepository<Role, String> {
    
    /**
     * Поиск роли по названию.
     *
     * @param name название.
     * @return роль.
     */
    Optional<Role> findByName(String name);
    
}
