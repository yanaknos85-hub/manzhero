package ru.sberbank.ditsib.transport.sudir.providers.authorization.request.dao;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.sudir.tables.AuthorizationRequest;
import ru.sber.transport.database.sudir.tables.records.AuthorizationRequestRecord;

import java.util.UUID;

/**
 * Репозиторий для работы с AuthorizationRequest в базе данных.
 */
public interface AuthorizationRequestRepository extends JooqRepository<AuthorizationRequest, AuthorizationRequestRecord, UUID> {
}
