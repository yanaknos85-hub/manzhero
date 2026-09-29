package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.DepLimit;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с лимитами департамента
 */
public interface DepLimitService {
    /**
     * Поиск лимита по идентификатору.
     *
     * @param id идентификатор.
     * @return лимит.
     */
    Optional<DepLimit> get(UUID id);

    /**
     * Сохранение лимита.
     *
     * @param limit лимит.
     */
    void save(DepLimit limit);

}
