package ru.sber.transport.fraud.monitoring.providers;


import ru.sber.transport.fraud.monitoring.model.TripPurpose;

import java.util.UUID;

/**
 * Провайдер данных о целях поездки.
 */
public interface TripPurposesGrpcProvider {

    /**
     * Получает данные о цели поездки
     *
     * @param id идентификатор цели поездки
     * @return данные о цели поездки
     */
    TripPurpose get(UUID id);

}
