package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.Position;

import java.util.UUID;

/**
 * Бизнес-логика системы должностей
 */
public interface PositionsGrpcProvider {

    /**
     * Получает данные о должности
     *
     * @param id идентификатор должности
     * @return данные о должности
     */
    Position get(UUID id);
}
