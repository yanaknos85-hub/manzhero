package ru.sberbank.ditsib.transport.sudir.providers.account.dao;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.sudir.tables.Account;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с УЗ.
 */
public interface AccountRepository extends JooqRepository<Account, AccountRecord, UUID> {
    
    /**
     * Получение активной УЗ по логину.
     *
     * @param personnelNumber personnelNumber.
     *
     * @return УЗ.
     */
    Optional<AccountRecord> findByActiveTrueAndPersonnelNumber(String personnelNumber);

    /**
     * Получение активной УЗ по логину.
     *
     * @param userId идентификатор пользователя.
     * @param active признак активности для получения.
     *
     * @return УЗ.
     */
    Optional<AccountRecord> findByActiveAndId(UUID userId, boolean active);
    
    /**
     * Получить УЗ по табельнику.
     *
     * @param personnelNumber ТН.
     * @return УЗ.
     */
    AccountRecord getByPersonnelNumber(String personnelNumber);
    
}
