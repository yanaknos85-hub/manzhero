package ru.sberbank.ditsib.transport.sudir.providers.refresh.dao;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.sudir.tables.Session;
import ru.sber.transport.database.sudir.tables.records.SessionRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for persisting sessions.
 */
public interface SessionRepository extends JooqRepository<Session, SessionRecord, UUID> {
    
    /**
     * Поиск сессии по токену.
     *
     * @param accessToken токен доступа.
     *
     * @return сессия.
     */
    Optional<SessionRecord> findByToken(String accessToken);
    
    /**
     * Поиск всех сессий УЗ.
     *
     * @param personnelNumber логин для поиска сессий.
     * @param expiration текущее время для проверки истечения.
     * @return сессии.
     */
    List<SessionRecord> findAllByAccountAndExpiredAtAfter(String personnelNumber, LocalDateTime expiration);
    
    /**
     * Поиск неистекшей сессии.
     *
     * @param id идентификатор.
     * @param expiration время для проверки.
     * @return неистёкшая сессия.
     */
    Optional<SessionRecord> findByIdAndExpiredAtAfter(UUID id, LocalDateTime expiration);
    
    List<SessionRecord> findAllByExpiredAtBefore(LocalDateTime now);
}
