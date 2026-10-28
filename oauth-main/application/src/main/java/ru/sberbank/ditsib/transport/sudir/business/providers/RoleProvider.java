package ru.sberbank.ditsib.transport.sudir.business.providers;

import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;

import java.util.Set;

/**
 * Провайдер ролей.
 */
public interface RoleProvider {
    
    /**
     * Получение ролей УЗ.
     *
     * @param account УЗ.
     * @return список ролей.
     */
    Set<Role> getRoles(Account account);

    /**
     * Getting roles by codes.
     *
     * @param roles set of codes.
     * @return set or roles.
     */
    Set<Role> getRoles(Set<String> roles);
}
