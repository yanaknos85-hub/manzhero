package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.Position;

import java.util.Optional;
import java.util.UUID;

public interface PositionService {


    /**
     * Get position by ID.
     *
     * @param id ID of position.
     * @return position.
     */
    Optional<Position> get(UUID id);

    /**
     * Delete position.
     *
     * @param position position.
     */
    void delete(Position position);

    /**
     * Save position.
     *
     * @param position position to save.
     */
    Position save(Position position);

    /**
     * Сохраняем должность, которую мы получим по grpc из сервиса corporate
     *
     * @param message Сообщение в случае ошибки
     * @param id      Идентификатор записи о должности
     */
    void saveGrpcEntity(String message, UUID id);
}
