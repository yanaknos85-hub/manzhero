package ru.sber.transport.fraud.monitoring.business;


import ru.sber.transport.fraud.monitoring.model.Position;

import java.util.UUID;

/**
 * Сервис работы с должностями
 */
public interface PositionsService {

    /**
     * Сохраняет должность
     *
     * @param source должность для сохранения
     * @return сохраненная должность
     */
    Position createOrUpdate(Position source);

    /**
     * Получает должность по идентификатору
     *
     * @param id идентификатор должности
     * @return должность
     */
    Position getExistedOrCreate(UUID id);
}
