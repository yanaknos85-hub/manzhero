package ru.sberbank.ditsib.transport.sudir.business.providers;

import ru.sberbank.ditsib.transport.sudir.business.model.Account;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер данных УЗ.
 */
public interface AccountProvider {
    
    /**
     * Получение УЗ по ТН.
     *
     * @param personnelNumber ТН.
     * @return УЗ.
     */
    Optional<Account> get(String personnelNumber);
    
    /**
     * Получение УЗ по идентификатору.
     *
     * @param id идентификатор.
     * @param active флаг активности. <code>null</code> исключает проверку активности.
     * @return УЗ.
     */
    Optional<Account> get(UUID id, Boolean active);

    /**
     * Получение УЗ по идентификатору.
     *
     * @param id идентификатор.
     * @return УЗ.
     */
    default Optional<Account> get(UUID id) {
        return get(id, null);
    }
    
    /**
     * Сохранение аккаунта.
     *
     * @param account УЗ.
     */

    void save(Account account);
}
